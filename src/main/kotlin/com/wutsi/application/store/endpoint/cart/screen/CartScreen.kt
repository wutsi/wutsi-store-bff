package com.wutsi.application.store.endpoint.cart.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.ProductListItem
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/cart")
class CartScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val cartApi: WutsiCartApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
) : AbstractQuery() {

    @PostMapping
    fun index(@RequestParam(name = "merchant-id") merchantId: Long): Widget {
        val tenant = tenantProvider.get()
        val merchant = accountApi.getAccount(merchantId).account
        val cart = cartApi.getCart(merchantId).cart
        val products = if (cart.products.isEmpty())
            emptyList()
        else
            catalogApi.searchProducts(
                SearchProductRequest(
                    productIds = cart.products.map { it.productId },
                    limit = cart.products.size
                )
            ).products

        val children = mutableListOf<WidgetAware>()
        if (products.isNotEmpty()) {
            children.addAll(products.map { toProductWidget(cart, it, tenant) })
            children.add(toPriceWidget(cart, products, tenant))
        } else {
            children.add(
                Container(
                    padding = 10.0,
                    alignment = Alignment.Center,
                    child = Text(getText("page.cart.empty"))
                )
            )
        }

        return Screen(
            id = Page.CART,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.cart.app-bar.title"),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    ProfileListItem(
                        model = sharedUIMapper.toAccountModel(merchant)
                    ),
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Flexible(
                        child = ListView(
                            children = children,
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER
                        )
                    )
                )
            )
        ).toWidget()
    }

    private fun toProductWidget(cart: Cart, product: ProductSummary, tenant: Tenant): WidgetAware {
        val quantity = getQuantity(cart, product.id)
        val maxQuantity = product.maxOrder ?: product.quantity

        return Container(
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOf(
                    ProductListItem(
                        model = sharedUIMapper.toProductModel(product, tenant),
                        action = gotoUrl("product?id=${product.id}")
                    ),
                    Row(
                        mainAxisAlignment = MainAxisAlignment.spaceAround,
                        crossAxisAlignment = CrossAxisAlignment.center,
                        children = listOf(
                            Container(
                                padding = 10.0,
                                width = 100.0,
                                child = if (product.quantity > 0)
                                    DropdownButton(
                                        name = "quantity",
                                        value = quantity.toString(),
                                        children = IntRange(1, maxQuantity).map {
                                            DropdownMenuItem(caption = it.toString(), value = it.toString())
                                        },
                                        outlinedBorder = false,
                                        action = executeCommand(
                                            urlBuilder.build("commands/update-cart?merchant-id=${product.accountId}&product-id=${product.id}")
                                        )
                                    )
                                else
                                    Row(
                                        children = listOf(
                                            Icon(code = Theme.ICON_CANCEL, color = Theme.COLOR_DANGER),
                                            Text(getText("page.cart.out-of-stock"), color = Theme.COLOR_DANGER)
                                        )
                                    )
                            ),
                            Button(
                                padding = 10.0,
                                stretched = false,
                                type = ButtonType.Outlined,
                                caption = getText("page.cart.button.remove"),
                                action = executeCommand(
                                    urlBuilder.build("commands/remove-from-cart?merchant-id=${product.accountId}&product-id=${product.id}")
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    private fun toPriceWidget(cart: Cart, products: List<ProductSummary>, tenant: Tenant): WidgetAware {
        val total = products.sumOf { getQuantity(cart, it.id) * (it.price ?: 0.0) }
        val fmt = DecimalFormat(tenant.monetaryFormat)
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
                                    getText(
                                        if (products.size == 1) "page.cart.total_1_item" else "page.cart.total_n_items",
                                        arrayOf(products.size.toString())
                                    ),
                                    bold = true,
                                    size = Theme.TEXT_SIZE_LARGE
                                ),
                            ),
                            Container(
                                padding = 10.0,
                                child = Text(
                                    fmt.format(total),
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
                        caption = getText("page.cart.button.checkout"),
                        action = executeCommand(urlBuilder.build("commands/create-order?merchant-id=${cart.merchantId}"))
                    )
                ),
            )
        )
    }

    private fun getQuantity(cart: Cart, productId: Long): Int =
        cart.products.find { it.productId == productId }?.quantity ?: 0
}
