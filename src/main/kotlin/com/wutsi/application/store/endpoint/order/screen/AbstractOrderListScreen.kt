package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.order.dto.FilterOrderRequest
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
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
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

abstract class AbstractOrderListScreen(
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    protected abstract fun getPageId(): String
    protected abstract fun getTitle(): String
    protected abstract fun getOrders(request: FilterOrderRequest?): List<OrderSummary>
    protected abstract fun getAction(order: OrderSummary): Action
    protected abstract fun getAccountId(order: OrderSummary): Long

    @PostMapping
    fun index(
        @RequestBody(required = false) request: FilterOrderRequest? = null
    ): Widget {
        val orders = getOrders(request)

        return Screen(
            id = getPageId(),
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getTitle(),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOfNotNull(
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
                        Flexible(child = toListView(orders))
                )
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }

    private fun toListView(orders: List<OrderSummary>): WidgetAware {
        val accountIds = orders.map { getAccountId(it) }.toSet()
        val accounts = accountApi.searchAccount(
            SearchAccountRequest(
                ids = accountIds.toList(),
                limit = accountIds.size
            )
        ).accounts.associateBy { it.id }

        val tenant = tenantProvider.get()
        val moneyFormat = DecimalFormat(tenant.monetaryFormat)
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateFormat, LocaleContextHolder.getLocale())

        return ListView(
            separatorColor = Theme.COLOR_DIVIDER,
            separator = true,
            children = orders.map {
                toOrderListItem(it, accounts[getAccountId(it)], moneyFormat, dateFormat)
            }
        )
    }

    private fun toOrderListItem(
        order: OrderSummary,
        account: AccountSummary?,
        moneyFormat: DecimalFormat,
        dateFormat: DateTimeFormatter,
    ) = ListItem(
        leading = account?.let {
            Avatar(
                model = sharedUIMapper.toAccountModel(it),
                radius = 24.0
            )
        },
        trailing = Text(moneyFormat.format(order.totalPrice), bold = true, color = Theme.COLOR_PRIMARY),
        caption = account?.displayName ?: "",
        subCaption = order.created.format(dateFormat) + " - " + getText("order.status.${order.status}"),
        action = getAction(order)
    )
}
