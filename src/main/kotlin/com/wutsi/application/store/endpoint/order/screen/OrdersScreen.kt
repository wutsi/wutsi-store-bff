package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.order.dto.FilterOrderRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.dto.SearchOrderRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
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
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/orders")
class OrdersScreen(
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
    private val orderApi: WutsiOrderApi,
) : AbstractQuery() {
    companion object {
        const val MAX_ORDERS = 100
    }

    @PostMapping
    fun index(
        @RequestParam(name = "merchant", required = false, defaultValue = "false") merchant: Boolean = false,
        @RequestBody(required = false) request: FilterOrderRequest? = null
    ): Widget {
        val tenant = tenantProvider.get()
        val orders = getOrders(merchant, request)
        val statuses = mutableListOf("")
        statuses.addAll(
            getOrderStatusList()
                .map { it.name to getText("order.status.${it.name}") }
                .sortedBy { it.second }
                .toMap()
                .map { it.key }
        )

        return Screen(
            id = Page.ORDERS,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.orders.app-bar.title"),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOfNotNull(
                    Container(
                        padding = 10.0,
                        child = DropdownButton(
                            name = "status",
                            value = request?.status ?: "",
                            children = statuses.map {
                                DropdownMenuItem(
                                    value = it,
                                    caption = if (it.isEmpty())
                                        getText("page.orders.all-orders")
                                    else
                                        getText("order.status.$it")
                                )
                            },
                            action = gotoUrl(
                                url = urlBuilder.build("/orders?merchant=$merchant"),
                                replacement = true
                            ),
                        )
                    ),

                    Center(
                        child = Container(
                            padding = 10.0,
                            child = Text(
                                caption = getText(
                                    if (orders.size <= 1)
                                        "page.order.list.count-1"
                                    else
                                        "page.order.list.count-n",
                                    arrayOf(orders.size.toString())
                                )
                            )
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),

                    if (orders.isEmpty())
                        null
                    else
                        Flexible(child = toListView(orders, tenant, !merchant))
                )
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }

    private fun toListView(orders: List<OrderSummary>, tenant: Tenant, showMerchantIcon: Boolean): WidgetAware {
        val accountIds = orders.flatMap { listOf(it.merchantId, it.accountId) }.toSet()
        val accounts = accountApi.searchAccount(
            SearchAccountRequest(
                ids = accountIds.toList(),
                limit = accountIds.size
            )
        ).accounts.associateBy { it.id }

        return ListView(
            separatorColor = Theme.COLOR_DIVIDER,
            separator = true,
            children = orders.map {
                toOrderWidget(it, accounts, tenant, showMerchantIcon)
            }
        )
    }

    private fun toOrderWidget(
        order: OrderSummary,
        accounts: Map<Long, AccountSummary>,
        tenant: Tenant,
        showMerchantIcon: Boolean
    ): WidgetAware {
        val moneyFormat = DecimalFormat(tenant.monetaryFormat)
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateFormat, LocaleContextHolder.getLocale())
        val merchant = accounts[order.merchantId]
        return ListItem(
            leading = if (showMerchantIcon)
                merchant?.let {
                    Avatar(
                        model = sharedUIMapper.toAccountModel(it),
                        radius = 24.0
                    )
                }
            else
                null,
            trailing = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.end,
                mainAxisSize = MainAxisSize.min,
                children = listOf(
                    Text(
                        caption = moneyFormat.format(order.totalPrice),
                        bold = true,
                        color = Theme.COLOR_PRIMARY,
                        size = Theme.TEXT_SIZE_SMALL
                    ),
                    Text(
                        caption = order.created.format(dateFormat),
                        size = Theme.TEXT_SIZE_SMALL
                    )
                ),
            ),
            caption = getText("page.order.number", arrayOf(order.id.takeLast(4))),
            subCaption = getText("order.status.${order.status}") +
                if (togglesProvider.isOrderPaymentEnabled()) " - " + getText("payment.status.${order.paymentStatus}") else "",
            action = gotoUrl(
                url = urlBuilder.build("/order?id=${order.id}")
            ),
        )
    }

    fun getOrders(merchant: Boolean, request: FilterOrderRequest?) = orderApi.searchOrders(
        request = SearchOrderRequest(
            merchantId = if (merchant) securityContext.currentAccountId() else null,
            accountId = if (!merchant) securityContext.currentAccountId() else null,
            status = request?.status?.let { listOf(it) }
                ?: getOrderStatusList().map { it.name },
            limit = MAX_ORDERS,
        )
    ).orders

    private fun getOrderStatusList(): List<OrderStatus> =
        OrderStatus.values().filter { it != OrderStatus.CREATED && it != OrderStatus.EXPIRED }


}
