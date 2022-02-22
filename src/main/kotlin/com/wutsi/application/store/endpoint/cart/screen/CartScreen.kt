package com.wutsi.application.store.endpoint.cart.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
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
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.flutter.sdui.enums.TextDecoration
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

        // Merchant
        val children = mutableListOf<WidgetAware>(
            Container(
                padding = 10.0,
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
                        Text(
                            caption = getText("page.cart.merchant"),
                            bold = true,
                            size = Theme.TEXT_SIZE_LARGE
                        ),
                        ProfileListItem(
                            model = sharedUIMapper.toAccountModel(merchant)
                        )
                    ),
                )
            ),
        )

        // Products
        if (products.isNotEmpty()) {
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.cart.products", arrayOf(cart.products.size.toString())),
                            bold = true,
                            size = Theme.TEXT_SIZE_LARGE
                        )
                    )
                )
            )
            products.map { toItemWidget(cart, it, tenant) }
                .forEach {
                    children.add(it)
                    children.add(Divider(color = Theme.COLOR_DIVIDER))
                }
        } else {
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(getText("page.cart.empty"))
                    )
                )
            )
        }

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
            )
        ).toWidget()
    }

    private fun toItemWidget(cart: Cart, product: ProductSummary, tenant: Tenant): WidgetAware {
        val quantity = getQuantity(cart, product.id)
        val maxQuantity = product.maxOrder ?: product.quantity
        val fmt = DecimalFormat(tenant.monetaryFormat)

        return Container(
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOf(
                    ListItem(
                        caption = product.title,
                        subCaption = null,
                        leading = product.thumbnail?.let { Image(url = it.url, width = 48.0, height = 48.8) },
                        trailing = Column(
                            mainAxisAlignment = MainAxisAlignment.start,
                            crossAxisAlignment = CrossAxisAlignment.end,
                            children = listOfNotNull(
                                Text(
                                    caption = fmt.format(quantity * (product.price ?: 0.0)),
                                    bold = true
                                ),
                                if (product.comparablePrice != null)
                                    Text(
                                        fmt.format(quantity * product.comparablePrice!!),
                                        size = Theme.TEXT_SIZE_SMALL,
                                        decoration = TextDecoration.Strikethrough
                                    )
                                else
                                    null,
                                if (quantity > 1)
                                    Text(
                                        getText("page.cart.price_each", arrayOf(fmt.format(product.price))),
                                        size = Theme.TEXT_SIZE_SMALL,
                                    )
                                else
                                    null
                            )
                        )
                    ),
                    Row(
                        mainAxisAlignment = MainAxisAlignment.start,
                        crossAxisAlignment = CrossAxisAlignment.center,
                        children = listOf(
                            Container(padding = 10.0),
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
                                            Text(
                                                getText("page.cart.out-of-stock"),
                                                color = Theme.COLOR_DANGER,
                                                size = Theme.TEXT_SIZE_SMALL
                                            )
                                        )
                                    )
                            ),
                            Button(
                                padding = 10.0,
                                stretched = false,
                                type = ButtonType.Text,
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
        val subTotal = products.sumOf { getQuantity(cart, it.id) * (it.comparablePrice ?: it.price ?: 0.0) }
        val total = products.sumOf { getQuantity(cart, it.id) * (it.price ?: 0.0) }
        val savings = total - subTotal

        val fmt = DecimalFormat(tenant.monetaryFormat)
        return Container(
            padding = 10.0,
            margin = 10.0,
            border = 1.0,
            borderColor = Theme.COLOR_DIVIDER,
            borderRadius = 5.0,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOfNotNull(
                    toPriceRow(getText("page.cart.sub-total", arrayOf(cart.products.size)), fmt.format(subTotal)),
                    if (subTotal > 0)
                        toPriceRow(
                            getText("page.cart.savings", arrayOf(cart.products.size)),
                            "-" + fmt.format(savings),
                            false,
                            Theme.COLOR_SUCCESS
                        )
                    else
                        null,
                    Container(
                        background = Theme.COLOR_PRIMARY_LIGHT,
                        child = toPriceRow(getText("page.cart.total"), fmt.format(total), true),
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

    private fun toPriceRow(
        name: String,
        value: String,
        bold: Boolean = false,
        color: String? = null
    ) = Row(
        children = listOf(
            Container(
                padding = 10.0,
                child = Text(name, bold = bold),
            ),
            Container(
                padding = 10.0,
                child = Text(
                    value,
                    bold = bold,
                    color = color,
                    alignment = TextAlignment.Right,
                )
            )
        ),
        mainAxisAlignment = MainAxisAlignment.spaceBetween
    )

    private fun getQuantity(cart: Cart, productId: Long): Int =
        cart.products.find { it.productId == productId }?.quantity ?: 0
}
