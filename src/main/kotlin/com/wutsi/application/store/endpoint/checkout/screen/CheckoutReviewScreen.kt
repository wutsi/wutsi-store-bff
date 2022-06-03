package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.model.ActionModel
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.AddressCard
import com.wutsi.application.shared.ui.OrderItemListItem
import com.wutsi.application.shared.ui.PriceSummaryCard
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.shared.ui.ShippingCard
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/checkout/review")
class CheckoutReviewScreen(
    private val orderApi: WutsiOrderApi,
    private val accountApi: WutsiAccountApi,
    private val catalogApi: WutsiCatalogApi,
    private val shippingApi: WutsiShippingApi,
    private val tenantProvider: TenantProvider,

    @Value("\${wutsi.application.login-url}") private val loginUrl: String
) : AbstractQuery() {

    @PostMapping
    fun index(@RequestParam(name = "order-id") orderId: String): Widget {
        val tenant = tenantProvider.get()
        val order = orderApi.getOrder(orderId).order
        val merchant = accountApi.getAccount(order.merchantId).account
        val products: Map<Long, ProductSummary> = catalogApi.searchProducts(
            request = SearchProductRequest(
                productIds = order.items.map { it.productId },
                limit = order.items.size
            )
        ).products.associateBy { it.id }

        // Merchant
        val children = mutableListOf<WidgetAware?>(
            toSectionWidget(
                padding = null,
                child = ProfileListItem(
                    model = sharedUIMapper.toAccountModel(merchant),
                    showAccountType = false
                )
            )
        )

        // Products
        children.add(toProductListWidget(order, products, tenant))

        // Shipping
        if (order.shippingId != null)
            children.add(toShippingWidget(order, tenant))

        // Price
        children.add(toPriceWidget(order, tenant))

        // Result
        return Screen(
            id = Page.CHECKOUT_REVIEW,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.review.app-bar.title"),
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children.filterNotNull()
                )
            ),
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
        ).toWidget()
    }

    private fun toItemWidget(item: OrderItem, product: ProductSummary, tenant: Tenant) = OrderItemListItem(
        model = sharedUIMapper.toOrderItemModel(item, product, tenant)
    )

    private fun toProductListWidget(order: Order, products: Map<Long, ProductSummary>, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        children.addAll(
            listOf(
                Container(
                    padding = 10.0,
                    child = Text(
                        caption = getText("page.checkout.review.products", arrayOf(order.items.size.toString())),
                        bold = true,
                        size = Theme.TEXT_SIZE_LARGE
                    )
                )
            )
        )
        order.items
            .map { toItemWidget(it, products[it.productId]!!, tenant) }
            .forEach {
                children.add(Divider(color = Theme.COLOR_DIVIDER))
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

    private fun toPriceWidget(order: Order, tenant: Tenant) = PriceSummaryCard(
        model = sharedUIMapper.toPriceSummaryModel(order, tenant),
        action = if (togglesProvider.isOrderPaymentEnabled())
            ActionModel(
                caption = getText(
                    "page.checkout.review.button.pay",
                    arrayOf(
                        DecimalFormat(tenant.monetaryFormat).format(order.totalPrice)
                    )
                ),
                action = gotoUrl(
                    url = urlBuilder.build("/checkout/payment?order-id=${order.id}")
                )
            )
        else
            ActionModel(
                caption = getText("page.checkout.review.button.submit"),
                action = executeCommand(
                    url = urlBuilder.build("commands/submit-order?order-id=${order.id}")
                )
            )
    )

    private fun toShippingWidget(order: Order, tenant: Tenant): WidgetAware? {
        if (order.shippingId == null)
            return null

        // Shipping method
        val children = mutableListOf<WidgetAware>()
        val shipping = shippingApi.getShipping(order.shippingId!!).shipping
        children.addAll(
            listOf(
                Text(
                    caption = getText("page.checkout.review.shipping", arrayOf(order.items.size.toString())),
                    bold = true,
                    size = Theme.TEXT_SIZE_LARGE
                ),
                Container(
                    padding = 5.0,
                    child = ShippingCard(
                        model = sharedUIMapper.toShippingModel(order, shipping, tenant)
                    )
                )
            )
        )

        // Shipping Address
        if (order.shippingAddress != null)
            children.addAll(
                listOf(
                    Container(padding = 10.0),
                    Text(getText("page.checkout.review.ship-to") + ":", bold = true),
                    Container(
                        padding = 5.0,
                        child = AddressCard(
                            model = sharedUIMapper.toAddressModel(order.shippingAddress!!)
                        )
                    )
                )
            )

        return toSectionWidget(
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = children
            )
        )
    }

    private fun getPaymentUrl(orderId: String): String {
        val me = accountApi.getAccount(securityContext.currentAccountId()).account
        return "?phone=" + encodeURLParam(me.phone!!.number) +
            "&icon=" + Theme.ICON_LOCK +
            "&screen-id=" + Page.CHECKOUT_PIN +
            "&title=" + encodeURLParam(getText("page.checkout-pin.title")) +
            "&sub-title=" + encodeURLParam(getText("page.checkout-pin.sub-title")) +
            "&auth=false" +
            "&return-to-route=false" +
            "&return-url=" + encodeURLParam(
            urlBuilder.build(
                "commands/pay-order?order-id=$orderId"
            )
        )
    }
}
