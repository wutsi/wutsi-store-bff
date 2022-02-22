package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.flutter.sdui.enums.TextAlignment
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
    private val sharedUIMapper: SharedUIMapper,
    private val tenantProvider: TenantProvider,
    private val urlBuilder: URLBuilder,
    private val securityContext: SecurityContext,

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
        val children = mutableListOf(
            ProfileListItem(
                model = sharedUIMapper.toAccountModel(merchant)
            ),
            Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
        )

        // Items
        children.addAll(
            order.items.map { toItemWidget(it, products[it.productId]!!, tenant) }
        )

        // Price
        children.addAll(
            listOf(
                Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                toPriceWidget(order, tenant)
            )
        )

        // Result
        return Screen(
            id = Page.CHECKOUT_REVIEW,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.review.app-bar.title"),
                automaticallyImplyLeading = false,
                leading = IconButton(
                    icon = Theme.ICON_CANCEL,
                    action = executeCommand(
                        url = urlBuilder.build(loginUrl, "commands/cancel-order?order-id=$orderId")
                    )
                )
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children
                )
            )
        ).toWidget()
    }

    private fun toItemWidget(item: OrderItem, product: ProductSummary, tenant: Tenant): WidgetAware {
        val fmt = DecimalFormat(tenant.monetaryFormat)
        return ListItem(
            caption = product.title,
            subCaption = getText("page.checkout.review.quantity", arrayOf(item.quantity)),
            leading = product.thumbnail?.let { Image(url = it.url, width = 48.0, height = 48.8) },
            trailing = Text(fmt.format(item.unitPrice * item.quantity)),
        )
    }

    private fun toPriceWidget(order: Order, tenant: Tenant): WidgetAware {
        val fmt = DecimalFormat(tenant.monetaryFormat)
        val totalPriceText = fmt.format(order.totalPrice)

        return Container(
            padding = 20.0,
            background = Theme.COLOR_PRIMARY_LIGHT,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOf(
                    Row(
                        children = listOf(
                            Container(
                                padding = 10.0,
                                child = Text(
                                    getText("page.checkout.review.total-price"),
                                    bold = true,
                                    size = Theme.TEXT_SIZE_LARGE
                                ),
                            ),
                            Container(
                                padding = 10.0,
                                child = Text(
                                    totalPriceText,
                                    bold = true,
                                    color = Theme.COLOR_PRIMARY,
                                    alignment = TextAlignment.Right,
                                    size = Theme.TEXT_SIZE_LARGE
                                )
                            )
                        ),
                        mainAxisAlignment = MainAxisAlignment.spaceBetween
                    ),
                    Container(padding = 10.0),
                    Button(
                        caption = getText("page.checkout.review.button.pay", arrayOf(totalPriceText)),
                        action = executeCommand(urlBuilder.build(getPaymentUrl(order.id)))
                    )
                ),
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
