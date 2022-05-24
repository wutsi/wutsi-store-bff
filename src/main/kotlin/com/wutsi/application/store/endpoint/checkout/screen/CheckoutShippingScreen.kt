package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.Product
import com.wutsi.ecommerce.shipping.dto.RateSummary
import com.wutsi.ecommerce.shipping.dto.SearchRateRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/checkout/shipping")
class CheckoutShippingScreen(
    private val shippingApi: WutsiShippingApi,
    private val orderApi: WutsiOrderApi,
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider
) : AbstractQuery() {

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String
    ): Widget {
        val tenant = tenantProvider.get()
        val account = securityContext.currentAccount()
        val order = orderApi.getOrder(orderId).order
        val products = catalogApi.searchProducts(
            SearchProductRequest(
                productIds = order.items.map { it.productId },
                limit = order.items.size
            )
        ).products
        val rates = shippingApi.searchRate(
            SearchRateRequest(
                country = account.country,
                cityId = account.cityId,
                accountId = order.merchantId,
                products = products.map {
                    Product(
                        productId = it.id,
                        productType = it.type,
                        quantity = it.quantity
                    )
                }
            )
        ).rates

        return Screen(
            id = Page.CHECKOUT_DELIVERY_METHOD,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.shipping.app-bar.title"),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = if (rates.isNotEmpty())
                    toShippingRateWidget(order, rates, tenant)
                else
                    listOf(
                        Center(
                            child = Container(
                                padding = 10.0,
                                alignment = Alignment.Center,
                                child = Text(
                                    getText("page.checkout.shipping.none"),
                                    size = Theme.TEXT_SIZE_LARGE,
                                    color = Theme.COLOR_DANGER
                                )
                            )
                        )
                    )
            )
        ).toWidget()
    }

    private fun toShippingRateWidget(order: Order, rates: List<RateSummary>, tenant: Tenant): List<WidgetAware> {
        val children = mutableListOf(
            Container(
                padding = 10.0,
                alignment = Alignment.Center,
                child = Text(
                    getText("page.checkout.shipping.message"),
                    size = Theme.TEXT_SIZE_LARGE,
                )
            ),
            Divider(height = 1.0, color = Theme.COLOR_DIVIDER),
        )

        children.addAll(
            rates.map {
                Container(
                    alignment = Alignment.TopLeft,
                    borderRadius = 4.0,
                    border = 1.0,
                    borderColor = Theme.COLOR_DIVIDER,
                    padding = 10.0,
                    margin = 10.0,
                    width = Double.MAX_VALUE, /* Full width */
                    child = Column(
                        mainAxisAlignment = MainAxisAlignment.start,
                        crossAxisAlignment = CrossAxisAlignment.start,
                        children = listOfNotNull(
                            Text(
                                caption = getText("shipping.type.${it.shippingType}") +
                                    " - " +
                                    formatRate(it.rate, tenant),
                                bold = true
                            ),
                            it.deliveryTime?.let {
                                Text(
                                    caption = getText(
                                        key = "page.checkout.shipping.delivery-delay",
                                        args = arrayOf(formatDeliveryTime(it))
                                    )
                                )
                            }
                        )
                    ),
                    action = executeCommand(
                        urlBuilder.build("commands/select-shipping-method?order-id=${order.id}&shipping-id=${it.shippingId}")
                    ),
                )
            }
        )
        return children
    }
}
