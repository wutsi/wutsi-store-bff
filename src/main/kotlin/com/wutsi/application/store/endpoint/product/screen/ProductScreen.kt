package com.wutsi.application.store.endpoint.product.screen

import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.model.AccountModel
import com.wutsi.application.shared.model.ProductModel
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.PhoneUtil
import com.wutsi.application.shared.service.StringUtil
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.shared.ui.ProductActionProvider
import com.wutsi.application.shared.ui.ProductCardType
import com.wutsi.application.shared.ui.ProductGridView
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.catalog.entity.ProductSort
import com.wutsi.ecommerce.catalog.entity.ProductStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.AspectRatio
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CarouselSlider
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.ExpandablePanel
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.MoneyText
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/product")
class ProductScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
    private val cityService: CityService
) : ProductActionProvider, AbstractQuery() {
    override fun getAction(model: ProductModel): Action =
        gotoUrl(
            url = urlBuilder.build("/product?id=${model.id}")
        )

    override fun getAction(model: AccountModel): Action? =
        gotoUrl(
            url = urlBuilder.build(shellUrl, "/profile?id=${model.id}")
        )

    @PostMapping
    fun index(@RequestParam id: Long, request: HttpServletRequest): Widget {
        val product = catalogApi.getProduct(id).product
        val merchant = accountApi.getAccount(product.accountId).account
        val tenant = tenantProvider.get()
        val cart = getCart(merchant)

        val children = mutableListOf<WidgetAware>(
            Container(
                padding = 10.0,
                background = Theme.COLOR_WHITE,
                child = Text(
                    caption = StringUtil.capitalizeFirstLetter(product.title),
                    size = Theme.TEXT_SIZE_LARGE,
                    bold = true
                )
            ),
        )

        // Pictures
        if (product.pictures.isNotEmpty())
            children.addAll(
                listOf(
                    toSectionWidget(
                        padding = null,
                        child = CarouselSlider(
                            viewportFraction = .9,
                            enableInfiniteScroll = false,
                            reverse = false,
                            height = 250.0,
                            children = product.pictures.map {
                                AspectRatio(
                                    aspectRatio = 8.0 / 10.0,
                                    child = Image(
                                        url = it.url,
                                        height = 300.0
                                    )
                                )
                            }
                        )
                    )
                )
            )

        children.add(
            toSectionWidget(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    mainAxisSize = MainAxisSize.min,
                    children = listOfNotNull(
                        // Price
                        if (product.price != null)
                            toPriceWidget(product, tenant)
                        else
                            null,

                        // Summary
                        if (!product.summary.isNullOrEmpty())
                            Column(
                                children = listOf(
                                    Text(product.summary!!),
                                )
                            )
                        else
                            null,

                        // Availability
                        Container(padding = 10.0),
                        toAvailabilityWidget(product),

                        // Cart
                        toCartWidget(merchant, product, cart),
                    )
                )
            )
        )

        // Product Details
        if (!product.description.isNullOrEmpty())
            children.add(
                Container(
                    padding = 10.0,
                    margin = 5.0,
                    border = 1.0,
                    borderColor = Theme.COLOR_GRAY_LIGHT,
                    background = Theme.COLOR_WHITE,
                    child = ExpandablePanel(
                        header = getText("page.product.product-details"),
                        expanded = Container(
                            padding = 10.0,
                            child = Text(product.description!!)
                        ),
                    )
                )
            )

        val shareUrl = "${tenant.webappUrl}/product?id=$id"
        val whatsappUrl = PhoneUtil.toWhatsAppUrl(merchant.whatsapp, shareUrl)
        children.addAll(
            listOfNotNull(
                toVendorWidget(product, merchant, whatsappUrl),
                toSimilarProductsWidget(product, tenant),
                toOtherProductsWidget(product, tenant),
            )
        )

        try {
            // Screen
            return Screen(
                id = Page.PRODUCT,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_WHITE,
                    foregroundColor = Theme.COLOR_BLACK,
                    actions = titleBarActions(
                        productId = id,
                        merchantId = merchant.id,
                        shareUrl = shareUrl,
                        whatsappUrl = whatsappUrl,
                        cart = cart
                    )
                ),
                child = Container(
                    child = ListView(
                        children = children,
                    )
                ),
                bottomNavigationBar = bottomNavigationBar(),
                backgroundColor = Theme.COLOR_GRAY_LIGHT
            ).toWidget()
        } finally {
            track(product, request)
        }
    }

    private fun track(product: Product, request: HttpServletRequest) {
        track(
            correlationId = UUID.randomUUID().toString(),
            page = Page.PRODUCT,
            event = EventType.VIEW,
            productId = product.id,
            merchantId = product.accountId,
            value = null,
            request = request
        )
    }

    private fun toCartWidget(merchant: Account, product: Product, cart: Cart?): WidgetAware? {
        if (cart == null)
            return null

        val item = cart.products.find { it.productId == product.id }
        return if (item != null)
            Row(
                children = listOf(
                    Icon(
                        code = Theme.ICON_CART,
                        color = Theme.COLOR_PRIMARY,
                        size = 16.0
                    ),
                    Container(padding = 5.0),
                    Text(getText("page.product.in-cart", arrayOf(item.quantity.toString())))
                )
            )
        else if (product.quantity > 0)
            Button(
                padding = 10.0,
                caption = getText("page.product.button.add-to-cart"),
                action = executeCommand(
                    url = urlBuilder.build("commands/add-to-cart?product-id=${product.id}&merchant-id=${merchant.id}")
                )
            )
        else
            null
    }

    private fun toAvailabilityWidget(product: Product): WidgetAware =
        Row(
            children = listOf(
                Icon(
                    code = if (product.quantity > 0) Theme.ICON_CHECK else Theme.ICON_CANCEL,
                    color = if (product.quantity > 0) Theme.COLOR_SUCCESS else Theme.COLOR_DANGER,
                    size = 16.0
                ),
                Container(padding = 5.0),
                Text(
                    caption = if (product.quantity > 0)
                        getText("page.product.in-stock")
                    else
                        getText("page.product.out-of-stock")
                )
            )
        )

    private fun toPriceWidget(product: Product, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        val price = product.price!!
        val comparablePrice = product.comparablePrice ?: 0.0
        val savings = comparablePrice - price
        val percent = (100.0 * savings / comparablePrice).toInt()
        val fmt = DecimalFormat(tenant.monetaryFormat)

        // Price
        children.add(
            MoneyText(
                currency = tenant.currency,
                color = Theme.COLOR_PRIMARY,
                valueFontSize = Theme.TEXT_SIZE_X_LARGE,
                value = price,
                numberFormat = tenant.numberFormat
            ),
        )

        if (savings > 0) {
            children.add(
                Text(
                    caption = fmt.format(comparablePrice),
                    decoration = TextDecoration.Strikethrough,
                    color = Theme.COLOR_GRAY,
                )
            )
            if (percent >= 1)
                children.add(
                    Text(
                        caption = getText("page.product.savings-percent", arrayOf(percent.toString())),
                        color = Theme.COLOR_SUCCESS,
                    )
                )
        }

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = children
        )
    }

    private fun toVendorWidget(product: Product, merchant: Account, whatsappUrl: String?): WidgetAware =
        toSectionWidget(
            child = Row(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOf(
                    Avatar(
                        radius = 24.0,
                        model = sharedUIMapper.toAccountModel(merchant)
                    ),
                    Container(padding = 10.0),
                    Container(
                        child = Column(
                            mainAxisAlignment = MainAxisAlignment.start,
                            crossAxisAlignment = CrossAxisAlignment.start,
                            mainAxisSize = MainAxisSize.min,
                            children = listOfNotNull(
                                Text(caption = merchant.displayName ?: "", bold = true),
                                Text(
                                    caption = if (merchant.category == null)
                                        toLocation(merchant)
                                    else
                                        merchant.category!!.title + " - " + toLocation(merchant),
                                    color = Theme.COLOR_GRAY
                                ),
                                whatsappUrl?.let {
                                    Button(
                                        type = if (togglesProvider.isCartEnabled()) ButtonType.Outlined else ButtonType.Elevated,
                                        stretched = false,
                                        padding = 10.0,
                                        caption = getText("page.product.write-to-merchant"),
                                        action = Action(
                                            type = ActionType.Navigate,
                                            url = it,
                                            trackEvent = EventType.CHAT.name,
                                            trackProductId = product.id.toString()
                                        ),
                                    )
                                }
                            )
                        )
                    )
                ),
            )
        )

    private fun toLocation(merchant: Account): String =
        sharedUIMapper.toLocationText(cityService.get(merchant.cityId), merchant.country)

    private fun toSimilarProductsWidget(product: Product, tenant: Tenant): WidgetAware? {
        // Get products
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                accountId = product.accountId,
                categoryIds = listOf(product.category.id, product.subCategory.id),
                status = ProductStatus.PUBLISHED.name,
                sortBy = ProductSort.RECOMMENDED.name,
                limit = 30
            )
        ).products.filter { it.id != product.id }
        if (products.isEmpty())
            return null

        // Sort - ensure all products in the same sub-categories... and from other merchants
        val similar = mutableListOf<ProductSummary>()
        similar.addAll(products.filter { it.subCategoryId == product.subCategory.id })
        similar.addAll(products.filter { it.subCategoryId != product.subCategory.id })

        // Component
        return toSectionWidget(
            padding = null,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(getText("page.product.similar-products"), bold = true)
                    ),
                    ProductGridView(
                        spacing = 5.0,
                        productsPerRow = 2,
                        models = similar.take(4)
                            .map { sharedUIMapper.toProductModel(it, tenant, null) },
                        actionProvider = this,
                        type = ProductCardType.SUMMARY,
                    )
                )
            )
        )
    }

    private fun toOtherProductsWidget(product: Product, tenant: Tenant): WidgetAware? {
        // Get products
        val categoryIds = listOf(product.category.id, product.subCategory.id)
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                accountId = product.accountId,
                status = ProductStatus.PUBLISHED.name,
                sortBy = ProductSort.RECOMMENDED.name,
                limit = 30
            )
        ).products.filter {
            it.id != product.id &&
                !categoryIds.contains(it.subCategoryId) &&
                !categoryIds.contains(it.categoryId)
        }
        if (products.isEmpty())
            return null

        // Component
        return toSectionWidget(
            padding = null,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOfNotNull(
                    Container(
                        padding = 10.0,
                        child = Text(getText("page.product.merchant-products"), bold = true)
                    ),
                    ProductGridView(
                        spacing = 5.0,
                        productsPerRow = 2,
                        models = products.take(4)
                            .map { sharedUIMapper.toProductModel(it, tenant, null) },
                        actionProvider = this,
                        type = ProductCardType.SUMMARY,
                    ),
                    if (products.size > 4)
                        Center(
                            child = Container(
                                padding = 10.0,
                                alignment = Alignment.Center,
                                child = Button(
                                    caption = getText("page.product.button.more-products"),
                                    padding = 10.0,
                                    type = ButtonType.Outlined,
                                    stretched = false,
                                    action = gotoUrl(
                                        url = urlBuilder.build(shellUrl, "/profile?id=${product.accountId}&tab=store")
                                    )
                                )
                            )
                        )
                    else
                        null
                )
            )
        )
    }
}
