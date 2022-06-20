package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.AddressCard
import com.wutsi.application.shared.ui.OrderItemListItem
import com.wutsi.application.shared.ui.PriceSummaryCard
import com.wutsi.application.shared.ui.ProfileCard
import com.wutsi.application.shared.ui.ProfileCardType
import com.wutsi.application.shared.ui.ShippingCard
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.order.entity.PaymentStatus
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DefaultTabController
import com.wutsi.flutter.sdui.Dialog
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.DynamicWidget
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.TabBar
import com.wutsi.flutter.sdui.TabBarView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/order")
class OrderScreen(
    private val orderApi: WutsiOrderApi,
    private val accountApi: WutsiAccountApi,
    private val catalogApi: WutsiCatalogApi,
    private val shippingApi: WutsiShippingApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam(name = "id") id: String): Widget {
        val tenant = tenantProvider.get()
        val order = orderApi.getOrder(id).order
        val shipping = if (togglesProvider.isShippingEnabled() && order.shippingId != null)
            shippingApi.getShipping(order.shippingId!!).shipping
        else
            null

        val tabs = TabBar(
            tabs = listOfNotNull(
                Text(getText("page.order.tab.products").uppercase(), bold = true),

                if (shipping != null)
                    Text(getText("page.order.tab.shipping").uppercase(), bold = true)
                else
                    null,

                Text(getText("page.order.tab.qr-code").uppercase(), bold = true),
            )
        )
        val tabViews = TabBarView(
            children = listOfNotNull(
                productsTab(order, tenant),

                if (shipping != null)
                    shippingTab(order, shipping, tenant)
                else
                    null,

                qrCodeTab(order)
            )
        )

        return DefaultTabController(
            length = tabs.tabs.size,
            child = Screen(
                id = Page.ORDER,
                backgroundColor = Theme.COLOR_GRAY_LIGHT,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_PRIMARY,
                    foregroundColor = Theme.COLOR_WHITE,
                    bottom = tabs,
                    title = getText("page.order.app-bar.title", arrayOf(order.id.uppercase().takeLast(4))),
                    actions = listOfNotNull(
                        getAppBarAction(order, shipping),
                        getShareAction(order, tenant),
                    )
                ),
                child = tabViews,
                bottomNavigationBar = bottomNavigationBar(),
            )
        ).toWidget()
    }

    private fun productsTab(order: Order, tenant: Tenant): WidgetAware {
        val customer = accountApi.getAccount(order.accountId).account
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateTimeFormat, LocaleContextHolder.getLocale())
        val children = mutableListOf<WidgetAware>()

        // Merchant
        children.add(
            toSectionWidget(
                child = ProfileCard(
                    model = sharedUIMapper.toAccountModel(
                        accountApi.getAccount(order.merchantId).account
                    ),
                    type = ProfileCardType.SUMMARY
                ),
                background = Theme.COLOR_WHITE
            )
        )

        // Status
        children.add(
            toSectionWidget(
                background = Theme.COLOR_WHITE,
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOfNotNull(
                        Text(
                            getText("page.order.number", arrayOf(order.id)),
                            size = Theme.TEXT_SIZE_SMALL
                        ),
                        Container(padding = 10.0),
                        toRow(
                            getText("page.order.from"),
                            Container(
                                action = gotoUrl(urlBuilder.build(shellUrl, "profile?id=${customer.id}")),
                                child = Text(
                                    caption = customer.displayName ?: "",
                                    color = Theme.COLOR_PRIMARY,
                                    decoration = TextDecoration.Underline
                                )
                            )
                        ),
                        toRow(
                            getText("page.order.date"),
                            order.created.format(dateFormat),
                        ),
                        toRow(
                            getText("page.order.status"),
                            Text(
                                caption = getText("order.status.${order.status}"),
                                color = if (OrderStatus.DELIVERED.name == order.status)
                                    Theme.COLOR_SUCCESS
                                else if (OrderStatus.CANCELLED.name == order.status)
                                    Theme.COLOR_DANGER
                                else
                                    null
                            ),
                        ),

                        if (togglesProvider.isOrderPaymentEnabled())
                            toRow(
                                getText("page.order.payment-status"),
                                Text(
                                    caption = getText("payment.status.${order.paymentStatus}"),
                                    color = if (PaymentStatus.PAID.name == order.paymentStatus)
                                        Theme.COLOR_SUCCESS
                                    else if (PaymentStatus.PENDING.name == order.status)
                                        Theme.COLOR_DANGER
                                    else
                                        null
                                ),
                            )
                        else
                            null,
                    ),
                )
            ),
        )

        // Products
        val products: Map<Long, ProductSummary> = catalogApi.searchProducts(
            request = SearchProductRequest(
                productIds = order.items.map { it.productId },
                limit = order.items.size
            )
        ).products.associateBy { it.id }
        children.add(toProductListWidget(order, products, tenant))

        // Price
        children.add(toPriceWidget(order, tenant))

        // Result
        return SingleChildScrollView(
            child = Column(
                children = children
            )
        )
    }

    private fun shippingTab(order: Order, shipping: Shipping, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()

        // Shipping Method
        children.add(
            toSectionWidget(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOfNotNull(
                        Text(getText("page.order.shipping-method"), bold = true, size = Theme.TEXT_SIZE_LARGE),
                        ShippingCard(
                            model = sharedUIMapper.toShippingModel(order, shipping, tenant),
                            showExpectedDeliveryDate = false
                        )
                    )
                )
            )
        )

        // Shipping Address
        if (order.shippingAddress != null)
            children.add(
                toSectionWidget(
                    child = Column(
                        mainAxisAlignment = MainAxisAlignment.start,
                        crossAxisAlignment = CrossAxisAlignment.start,
                        children = listOfNotNull(
                            Text(getText("page.order.shipping-address"), bold = true, size = Theme.TEXT_SIZE_LARGE),
                            AddressCard(
                                model = sharedUIMapper.toAddressModel(order.shippingAddress!!),
                                showPostalAddress = shipping.type != ShippingType.IN_STORE_PICKUP.name
                            )
                        )
                    )
                )
            )

        // Shipping Instruction
        if (!shipping.message.isNullOrEmpty())
            children.add(
                toSectionWidget(
                    child = Column(
                        mainAxisAlignment = MainAxisAlignment.start,
                        crossAxisAlignment = CrossAxisAlignment.start,
                        children = listOf(
                            Text(
                                getText("page.order.shipping-instructions"),
                                bold = true,
                                size = Theme.TEXT_SIZE_LARGE
                            ),
                            Text(shipping.message!!)
                        )
                    )
                )

            )

        // Result
        return SingleChildScrollView(
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = children
            )
        )
    }

    private fun qrCodeTab(order: Order) = toSectionWidget(
        child = DynamicWidget(
            url = urlBuilder.build("order/qr-code-widget?id=${order.id}")
        )
    )

    private fun toProductListWidget(order: Order, products: Map<Long, ProductSummary>, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        var i = 0
        order.items.map { toItemWidget(it, products[it.productId]!!, tenant) }
            .forEach {
                if (i++ > 0)
                    children.add(Divider(color = Theme.COLOR_DIVIDER, height = 1.0))
                children.add(it)
            }
        return toSectionWidget(
            padding = null,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = children
            )
        )
    }

    private fun toItemWidget(item: OrderItem, product: ProductSummary, tenant: Tenant) = OrderItemListItem(
        model = sharedUIMapper.toOrderItemModel(item, product, tenant)
    )

    private fun toPriceWidget(order: Order, tenant: Tenant) = PriceSummaryCard(
        model = sharedUIMapper.toPriceSummaryModel(order, tenant),
        showPaymentStatus = togglesProvider.isOrderPaymentEnabled(),
    )

    private fun toRow(name: String, value: String): WidgetAware =
        toRow(name, Text(value))

    private fun toRow(name: String, value: WidgetAware?): WidgetAware =
        Row(
            children = listOf(
                Flexible(
                    flex = 2,
                    child = Container(
                        padding = 5.0,
                        child = Text(name, bold = true, size = Theme.TEXT_SIZE_SMALL)
                    )
                ),
                Flexible(
                    flex = 5,
                    child = Container(
                        padding = 5.0,
                        child = value
                    )
                )
            )
        )

    private fun getShareAction(order: Order, tenant: Tenant): WidgetAware =
        Container(
            padding = 4.0,
            child = CircleAvatar(
                radius = 20.0,
                backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                child = IconButton(
                    icon = Theme.ICON_SHARE,
                    size = 20.0,
                    action = Action(
                        type = ActionType.Share,
                        url = "${tenant.webappUrl}/order?id=${order.id}",
                    )
                )
            )
        )

    private fun getAppBarAction(order: Order, shipping: Shipping?): WidgetAware? {
        if (order.merchantId != securityContext.currentAccountId())
            return null

        val buttons = getAppBarButtons(order, shipping)
        if (buttons.isEmpty())
            return null

        return Container(
            padding = 4.0,
            child = CircleAvatar(
                radius = 20.0,
                backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                child = IconButton(
                    icon = Theme.ICON_MORE,
                    size = 20.0,
                    action = Action(
                        type = ActionType.Prompt,
                        prompt = Dialog(
                            type = DialogType.Confirm,
                            actions = buttons
                        ).toWidget()
                    )
                )
            )
        )
    }

    private fun getAppBarButtons(order: Order, shipping: Shipping?): List<WidgetAware> {
        val children = mutableListOf<WidgetAware>()
        if (order.status == OrderStatus.OPENED.name)
            children.add(
                Button(
                    caption = getText("page.order.button.close"),
                    action = gotoUrl(urlBuilder.build("/order/close?id=${order.id}"))
                ),
            )
        else if (isAvailableForDelivery(order))
            children.add(
                Button(
                    caption = getText("page.order.button.deliver"),
                    action = gotoUrl(urlBuilder.build("/order/deliver?id=${order.id}"))
                )
            )
        else if (isAvailableForLocalDelivery(order, shipping))
            children.add(
                Button(
                    caption = getText("page.order.button.start-delivery"),
                    action = gotoUrl(urlBuilder.build("/order/start-delivery?id=${order.id}"))
                )
            )

        if (canCancel(order))
            children.add(
                Button(
                    type = ButtonType.Outlined,
                    caption = getText("page.order.button.cancel"),
                    action = gotoUrl(urlBuilder.build("/order/cancel?id=${order.id}"))
                )
            )

        return children
    }

    private fun canCancel(order: Order): Boolean =
        order.status != OrderStatus.EXPIRED.name &&
            order.status != OrderStatus.CANCELLED.name &&
            order.status != OrderStatus.DELIVERED.name

    private fun isAvailableForDelivery(order: Order): Boolean =
        order.status == OrderStatus.READY_FOR_PICKUP.name ||
            order.status == OrderStatus.IN_TRANSIT.name

    private fun isAvailableForLocalDelivery(order: Order, shipping: Shipping?): Boolean =
        togglesProvider.isShippingLocalDeliveryEnabled() &&
            shipping != null &&
            shipping.type == ShippingType.LOCAL_DELIVERY.name &&
            order.status == OrderStatus.DONE.name
}
