package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.OrderItemListItem
import com.wutsi.application.shared.ui.PriceSummaryCard
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
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
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
    private val sharedUIMapper: SharedUIMapper,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {

    @PostMapping
    fun index(
        @RequestParam(name = "id") id: String,
        @RequestParam(name = "hide-merchant", required = false) hideMerchant: Boolean = false,
        @RequestParam(name = "hide-customer", required = false) hideCustomer: Boolean = false
    ): Widget {
        val tenant = tenantProvider.get()
        val order = orderApi.getOrder(id).order

        val children = mutableListOf<WidgetAware?>()

        // ID, Date
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateTimeFormat)
        children.addAll(
            listOf(
                toRow(getText("page.order.order-id"), order.id),
                toRow(getText("page.order.order-date"), order.created.format(dateFormat)),
                toRow(getText("page.order.status"), getText("order.status.${order.status}")),
            )
        )

        // Merchant
        val accounts = accountApi.searchAccount(
            request = SearchAccountRequest(
                ids = listOf(order.merchantId, order.accountId),
                limit = 2
            )
        ).accounts.associateBy { it.id }
        if (!hideMerchant)
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    toAccountWidget(getText("page.order.merchant"), accounts[order.merchantId]),
                )
            )
        if (!hideCustomer)
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    toAccountWidget(getText("page.order.customer"), accounts[order.accountId])
                )
            )

        // Products
        val products: Map<Long, ProductSummary> = catalogApi.searchProducts(
            request = SearchProductRequest(
                productIds = order.items.map { it.productId },
                limit = order.items.size
            )
        ).products.associateBy { it.id }
        children.addAll(
            listOf(
                Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                Container(
                    padding = 10.0,
                    child = Text(
                        caption = getText("page.order.products", arrayOf(order.items.size.toString())),
                        bold = true,
                        size = Theme.TEXT_SIZE_LARGE
                    )
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

    private fun toAccountWidget(title: String, account: AccountSummary?) = account?.let {
        Container(
            padding = 10.0,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Text(
                        caption = title,
                        bold = true,
                        size = Theme.TEXT_SIZE_LARGE
                    ),
                    ProfileListItem(
                        model = sharedUIMapper.toAccountModel(it),
                        action = gotoUrl(urlBuilder.build(shellUrl, "/profile?id=${account.id}"))
                    )
                ),
            )
        )
    }

    private fun toItemWidget(item: OrderItem, product: ProductSummary, tenant: Tenant) = OrderItemListItem(
        model = sharedUIMapper.toOrderItemModel(item, product, tenant)
    )

    private fun toPriceWidget(order: Order, tenant: Tenant) = PriceSummaryCard(
        model = sharedUIMapper.toPriceSummaryModel(order, tenant),
    )

    private fun toRow(name: String, value: String) = Row(
        children = listOf(
            Flexible(
                flex = 1,
                child = Container(
                    padding = 10.0,
                    child = Text(name, bold = true)
                )
            ),
            Flexible(
                flex = 2,
                child = Container(
                    padding = 10.0,
                    child = Text(value, size = Theme.TEXT_SIZE_SMALL)
                )
            )
        )
    )
}
