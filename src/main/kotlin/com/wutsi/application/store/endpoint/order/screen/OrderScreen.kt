package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.AddressCard
import com.wutsi.application.shared.ui.OrderItemListItem
import com.wutsi.application.shared.ui.PriceSummaryCard
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
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.tenant.dto.Tenant
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
        val accounts = accountApi.searchAccount(
            request = SearchAccountRequest(
                ids = listOf(order.merchantId, order.accountId),
                limit = 2
            )
        ).accounts.associateBy { it.id }

        val children = mutableListOf<WidgetAware?>()

        // ID, Date
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateTimeFormat)
        children.addAll(
            listOfNotNull(
                toRow(getText("page.order.order-id"), order.id),
                toRow(getText("page.order.order-date"), order.created.format(dateFormat)),
                toRow(getText("page.order.status"), getText("order.status.${order.status}")),
                toRow(getText("page.order.customer"), toAccountWidget(accounts[order.accountId])),

                if (order.shippingId != null)
                    toRow(
                        getText("page.order.shipping"),
                        ShippingCard(
                            model = sharedUIMapper.toShippingModel(
                                order = order,
                                shipping = shippingApi.getShipping(order.shippingId!!).shipping,
                                tenant = tenant,
                            ),
                            textSize = Theme.TEXT_SIZE_SMALL,
                            showShoppingInstructions = false,
                            showExpectedDeliveryDate = false
                        )
                    )
                else
                    null,

                if (order.shippingId != null && order.shippingAddress != null)
                    toRow(
                        getText("page.order.ship-to"),
                        AddressCard(
                            model = sharedUIMapper.toAddressModel(order.shippingAddress!!),
                            textSize = Theme.TEXT_SIZE_SMALL
                        ),
                    )
                else
                    null
            )
        )

        // Products
        val products: Map<Long, ProductSummary> = catalogApi.searchProducts(
            request = SearchProductRequest(
                productIds = order.items.map { it.productId },
                limit = order.items.size
            )
        ).products.associateBy { it.id }
        children.add(
            Container(
                padding = 10.0,
                child = Text(
                    caption = getText("page.order.products", arrayOf(order.items.size.toString())),
                    bold = true,
                    size = Theme.TEXT_SIZE_LARGE
                )
            )
        )
        order.items.map { toItemWidget(it, products[it.productId]!!, tenant) }
            .forEach {
                children.add(it)
                children.add(Divider(color = Theme.COLOR_DIVIDER))
            }

        // Price
        children.add(toPriceWidget(order, tenant))

        // Result
        return Screen(
            id = Page.ORDER,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.order.app-bar.title"),
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children.filterNotNull()
                )
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }

    private fun toShippingWidget(order: Order, tenant: Tenant): WidgetAware {
        val shipping = shippingApi.getShipping(order.shippingId!!).shipping
        return ShippingCard(
            model = sharedUIMapper.toShippingModel(order, shipping, tenant)
        )
    }

    private fun toAccountWidget(account: AccountSummary?) = account?.let {
        Container(
            child = Text(
                caption = it.displayName ?: "",
                color = Theme.COLOR_PRIMARY,
                decoration = TextDecoration.Underline,
                size = Theme.TEXT_SIZE_SMALL
            ),
            action = gotoUrl(urlBuilder.build(shellUrl, "/profile?id=${account.id}"))
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
        toRow(name, Text(value, size = Theme.TEXT_SIZE_SMALL))

    private fun toRow(name: String, value: WidgetAware?): WidgetAware =
        Column(
            children = listOf(
                Row(
                    children = listOf(
                        Flexible(
                            flex = 1,
                            child = Container(
                                alignment = Alignment.TopLeft,
                                padding = 5.0,
                                child = Text(name, bold = true, size = Theme.TEXT_SIZE_SMALL),
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
                ),
                Divider(color = Theme.COLOR_DIVIDER, height = 1.0)
            )
        )
}
