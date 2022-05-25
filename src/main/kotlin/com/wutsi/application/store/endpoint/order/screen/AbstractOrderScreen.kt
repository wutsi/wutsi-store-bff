package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.AddressCard
import com.wutsi.application.shared.ui.OrderItemListItem
import com.wutsi.application.shared.ui.PriceSummaryCard
import com.wutsi.application.shared.ui.ProfileCard
import com.wutsi.application.shared.ui.ProfileCardType
import com.wutsi.application.shared.ui.ShippingCard
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DefaultTabController
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.DynamicWidget
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.TabBar
import com.wutsi.flutter.sdui.TabBarView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.format.DateTimeFormatter

abstract class AbstractOrderScreen(
    private val orderApi: WutsiOrderApi,
    private val accountApi: WutsiAccountApi,
    private val catalogApi: WutsiCatalogApi,
    private val shippingApi: WutsiShippingApi,
    private val tenantProvider: TenantProvider,
    private val cityService: CityService,
) : AbstractQuery() {
    protected abstract fun getPageId(): String
    protected abstract fun showMerchantInfo(): Boolean
    protected open fun getPageTitle(order: Order): String =
        getText("page.order.app-bar.title", arrayOf(order.id.uppercase().takeLast(4)))

    @PostMapping
    fun index(@RequestParam(name = "id") id: String): Widget {
        val tenant = tenantProvider.get()
        val order = orderApi.getOrder(id).order

        val tabs = TabBar(
            tabs = listOfNotNull(
                Text(getText("page.order.tab.products").uppercase(), bold = true),

                if (order.shippingId != null)
                    Text(getText("page.order.tab.shipping").uppercase(), bold = true)
                else
                    null,

                Text(getText("page.order.tab.qr-code").uppercase(), bold = true),
            )
        )
        val tabViews = TabBarView(
            children = listOfNotNull(
                productsTab(order, tenant),

                if (order.shippingId != null)
                    shippingTab(order, tenant)
                else
                    null,

                qrCodeTab(order)
            )
        )

        return DefaultTabController(
            length = tabs.tabs.size,
            child = Screen(
                id = getPageId(),
                backgroundColor = Theme.COLOR_WHITE,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_PRIMARY,
                    foregroundColor = Theme.COLOR_WHITE,
                    bottom = tabs,
                    title = getPageTitle(order),
                ),
                child = tabViews,
                bottomNavigationBar = bottomNavigationBar()
            )
        ).toWidget()
    }

    private fun productsTab(order: Order, tenant: Tenant): WidgetAware {
        val customer = accountApi.getAccount(order.accountId).account
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateTimeFormat, LocaleContextHolder.getLocale())
        val children = mutableListOf<WidgetAware>()

        // Merchant
        if (showMerchantInfo())
            children.addAll(
                listOf(
                    ProfileCard(
                        model = sharedUIMapper.toAccountModel(
                            accountApi.getAccount(order.merchantId).account
                        ),
                        type = ProfileCardType.SUMMARY
                    ),
                    Divider(color = Theme.COLOR_DIVIDER)
                )
            )

        // Status
        children.add(
            Container(
                background = Theme.COLOR_GRAY_LIGHT,
                padding = 10.0,
                margin = 10.0,
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
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
                                getText("order.status.${order.status}"),
                                color = if (OrderStatus.DONE.name == order.status)
                                    Theme.COLOR_SUCCESS
                                else if (OrderStatus.CANCELLED.name == order.status)
                                    Theme.COLOR_DANGER
                                else
                                    null
                            ),
                        ),
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
        order.items.map { toItemWidget(it, products[it.productId]!!, tenant) }
            .forEach {
                children.add(Divider(color = Theme.COLOR_DIVIDER, height = 1.0))
                children.add(it)
            }

        // Price
        children.add(toPriceWidget(order, tenant))

        // Result
        return SingleChildScrollView(
            child = Column(children = children)
        )
    }

    private fun shippingTab(order: Order, tenant: Tenant): WidgetAware {
        val shipping = shippingApi.getShipping(order.shippingId!!).shipping
        val children = mutableListOf<WidgetAware>()

        // Shipping Method
        children.addAll(
            listOf(
                Text(getText("page.order.shipping-method"), bold = true, size = Theme.TEXT_SIZE_LARGE),
                ShippingCard(
                    model = sharedUIMapper.toShippingModel(order, shipping, tenant),
                    showExpectedDeliveryDate = false
                )
            )
        )

//        // Pickup Location
//        if (shipping.type == ShippingType.LOCAL_PICKUP.name) {
//            val city = shipping.cityId?.let { cityService.get(it) }
//            children.addAll(
//                listOfNotNull(
//                    Container(padding = 10.0),
//                    Text(getText("page.order.shipping.pickup-address"), bold = true, size = Theme.TEXT_SIZE_LARGE),
//                    shipping.street?.let { Text(it) },
//                    city?.let {
//                        Text(
//                            "${city.name}, " +
//                                Locale("en", city.country).getDisplayCountry(LocaleContextHolder.getLocale())
//                        )
//                    }
//                )
//            )
//        }

        // Shipping Address
        if (order.shippingAddress != null)
            children.addAll(
                listOfNotNull(
                    Container(padding = 10.0),
                    Text(getText("page.order.shipping-address"), bold = true, size = Theme.TEXT_SIZE_LARGE),
                    order.shippingAddress?.let {
                        AddressCard(model = sharedUIMapper.toAddressModel(it))
                    }
                )
            )

        // Shipping Instruction
        if (!shipping.message.isNullOrEmpty())
            children.addAll(
                listOf(
                    Container(padding = 10.0),
                    Text(getText("page.order.shipping-instructions"), bold = true, size = Theme.TEXT_SIZE_LARGE),
                    Text(shipping.message!!)
                )
            )

        // Result
        return SingleChildScrollView(
            child = Container(
                padding = 10.0,
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children
                )
            )
        )
    }

    private fun qrCodeTab(order: Order) = DynamicWidget(
        url = urlBuilder.build("order/qr-code-widget?id=${order.id}")
    )

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
                    flex = 1,
                    child = Container(
                        padding = 5.0,
                        child = Text(name, bold = true, size = Theme.TEXT_SIZE_SMALL)
                    )
                ),
                Flexible(
                    flex = 3,
                    child = Container(
                        padding = 5.0,
                        child = value
                    )
                )
            )
        )
}
