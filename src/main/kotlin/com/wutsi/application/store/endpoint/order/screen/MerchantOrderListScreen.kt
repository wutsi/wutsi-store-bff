package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.order.command.FilterOrderRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.dto.SearchOrderRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountRequest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/orders/merchant")
class MerchantOrderListScreen(
    private val urlBuilder: URLBuilder,
    private val accountApi: WutsiAccountApi,
    private val orderApi: WutsiOrderApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
    private val securityContext: SecurityContext
) : AbstractQuery() {

    @PostMapping
    fun index(
        @RequestBody(required = false) request: FilterOrderRequest = FilterOrderRequest()
    ): Widget {
        val orders = orderApi.searchOrders(
            SearchOrderRequest(
                merchantId = securityContext.currentAccountId(),
                status = request.status?.let { listOf(request.status.name) } ?: emptyList(),
                limit = 100
            )
        ).orders

        return Screen(
            id = Page.ORDER_LIST_MERCHANT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.order.merchant.app-bar.title"),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOfNotNull(
                    Container(
                        padding = 10.0,
                        child = DropdownButton(
                            value = request.status?.name ?: OrderStatus.READY.name,
                            name = "status",
                            children = listOf(
                                orderStatusWidget(OrderStatus.READY),
                                orderStatusWidget(OrderStatus.PROCESSING),
                                orderStatusWidget(OrderStatus.COMPLETED),
                                orderStatusWidget(OrderStatus.CANCELLED),
                            ),
                            action = gotoUrl(
                                urlBuilder.build("/orders/merchant")
                            )
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = if (orders.isEmpty())
                                getText("page.order.merchant.no-order")
                            else
                                getText("page.order.merchant.order-count", arrayOf(orders.size.toString()))
                        )
                    ),
                    if (orders.isEmpty())
                        null
                    else
                        Flexible(child = toListView(orders))
                )
            )
        ).toWidget()
    }

    private fun toListView(orders: List<OrderSummary>): WidgetAware {
        val customerIds = orders.map { it.accountId }.toSet()
        val customers = accountApi.searchAccount(
            SearchAccountRequest(
                ids = customerIds.toList(),
                limit = customerIds.size
            )
        ).accounts.associateBy { it.id }

        val tenant = tenantProvider.get()
        val moneyFormat = DecimalFormat(tenant.monetaryFormat)
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateTimeFormat, LocaleContextHolder.getLocale())

        return ListView(
            separatorColor = Theme.COLOR_DIVIDER,
            separator = true,
            children = orders.map {
                toOrderListItem(it, customers[it.accountId], moneyFormat, dateFormat)
            }
        )
    }

    private fun toOrderListItem(
        order: OrderSummary,
        customer: AccountSummary?,
        moneyFormat: DecimalFormat,
        dateFormat: DateTimeFormatter
    ) = ListItem(
        leading = customer?.let {
            Avatar(
                model = sharedUIMapper.toAccountModel(it),
                radius = 24.0
            )
        },
        trailing = Text(moneyFormat.format(order.totalPrice), bold = true, color = Theme.COLOR_PRIMARY),
        caption = customer?.displayName ?: "",
        subCaption = order.created.format(dateFormat),
        action = gotoUrl(urlBuilder.build("/order?id=${order.id}&hide-merchant=true"))
    )

    private fun orderStatusWidget(status: OrderStatus) = DropdownMenuItem(
        value = status.name,
        caption = getText("order.status.$status")
    )
}
