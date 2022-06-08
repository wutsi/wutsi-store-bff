package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.order.dto.FilterOrderRequest
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
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
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.findAnnotation

abstract class AbstractOrdersScreen(
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    companion object {
        const val MAX_ORDERS = 100
    }

    protected abstract fun getPageId(): String
    protected abstract fun getTitle(): String
    protected abstract fun getOrders(request: FilterOrderRequest?): List<OrderSummary>
    protected abstract fun getAction(order: OrderSummary): Action
    protected abstract fun getAccountId(order: OrderSummary): Long?

    @PostMapping
    fun index(
        @RequestBody(required = false) request: FilterOrderRequest? = null
    ): Widget {
        val tenant = tenantProvider.get()
        val orders = getOrders(request)
        val statuses = mutableListOf("")
        statuses.addAll(
            getOrderStatusList()
                .map { it.name to getText("order.status.${it.name}") }
                .sortedBy { it.second }
                .toMap()
                .map { it.key }
        )

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
                                url = urlBuilder.build(getUrl()),
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
                        Flexible(child = toListView(orders, tenant))
                )
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }

    private fun getUrl(): String =
        this::class.findAnnotation<RequestMapping>()!!.value[0]

    private fun toListView(orders: List<OrderSummary>, tenant: Tenant): WidgetAware {
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
                toOrderWidget(it, accounts, tenant)
            }
        )
    }

    private fun toOrderWidget(order: OrderSummary, accounts: Map<Long, AccountSummary>, tenant: Tenant): WidgetAware {
        val moneyFormat = DecimalFormat(tenant.monetaryFormat)
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateFormat, LocaleContextHolder.getLocale())
        val merchant = getAccountId(order)?.let { accounts[it] }
        return ListItem(
            leading = merchant?.let {
                Avatar(
                    model = sharedUIMapper.toAccountModel(it),
                    radius = 24.0
                )
            },
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
            action = getAction(order),
        )
    }

    protected fun getOrderStatusList(): List<OrderStatus> =
        OrderStatus.values().filter { it != OrderStatus.CREATED && it != OrderStatus.EXPIRED }
}
