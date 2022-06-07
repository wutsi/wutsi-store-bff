package com.wutsi.application.store.endpoint.cart.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.model.ActionModel
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.CartItemListItem
import com.wutsi.application.shared.ui.PriceSummaryCard
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.catalog.entity.ProductStatus
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cart")
class CartScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {

    @PostMapping
    fun index(@RequestParam(name = "merchant-id") merchantId: Long): Widget {
        val tenant = tenantProvider.get()
        val merchant = accountApi.getAccount(merchantId).account

        // Merchant
        val children = mutableListOf<WidgetAware>(
            toSectionWidget(
                padding = null,
                child = ProfileListItem(
                    model = sharedUIMapper.toAccountModel(merchant),
                    showAccountType = false
                )
            )
        )

        // Products
        val cart = cartApi.getCart(merchantId).cart
        val products = if (cart.products.isEmpty())
            emptyList()
        else
            catalogApi.searchProducts(
                SearchProductRequest(
                    productIds = cart.products.map { it.productId },
                    status = ProductStatus.PUBLISHED.name,
                    limit = cart.products.size
                )
            ).products
        children.add(toCartItemListWidget(cart, products, tenant))

        // Price
        if (products.isNotEmpty()) {
            children.add(toPriceWidget(cart, products, tenant))
        }

        return Screen(
            id = Page.CART,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.cart.app-bar.title"),
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children
                )
            ),
            backgroundColor = Theme.COLOR_GRAY_LIGHT
        ).toWidget()
    }

    private fun toCartItemListWidget(cart: Cart, products: List<ProductSummary>, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        val cartItems = products.map { toCartItemWidget(cart, it, tenant) }
            .filterNotNull()
        if (cartItems.isEmpty())
            return toSectionWidget(
                Container(
                    padding = 10.0,
                    alignment = Alignment.Center,
                    child = Text(getText("page.cart.empty")),
                )
            )

        children.add(
            Container(
                padding = 10.0,
                child = Text(
                    caption = getText("page.cart.products", arrayOf(cart.products.size.toString())),
                    bold = true,
                    size = Theme.TEXT_SIZE_LARGE
                )
            )
        )
        cartItems.forEach {
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

    private fun toCartItemWidget(cart: Cart, product: ProductSummary, tenant: Tenant): WidgetAware? {
        val cartItem = cart.products.find { it.productId == product.id }
            ?: return null

        return CartItemListItem(
            model = sharedUIMapper.toCartItemModel(cartItem, product, tenant),
            changeQuantityAction = ActionModel(
                caption = "",
                action = executeCommand(
                    urlBuilder.build("commands/update-cart?merchant-id=${product.accountId}&product-id=${product.id}")
                )
            ),
            removeAction = ActionModel(
                caption = getText("page.cart.button.remove"),
                executeCommand(
                    urlBuilder.build("commands/remove-from-cart?merchant-id=${product.accountId}&product-id=${product.id}")
                )
            )
        )
    }

    private fun toPriceWidget(cart: Cart, products: List<ProductSummary>, tenant: Tenant): WidgetAware =
        PriceSummaryCard(
            model = sharedUIMapper.toPriceSummaryModel(cart, products, tenant),
            action = ActionModel(
                caption = getText("page.cart.button.checkout"),
                action = executeCommand(urlBuilder.build("commands/create-order?merchant-id=${cart.merchantId}"))
            )
        )
}
