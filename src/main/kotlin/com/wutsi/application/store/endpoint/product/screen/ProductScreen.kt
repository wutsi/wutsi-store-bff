package com.wutsi.application.store.endpoint.product.screen

import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.PhoneUtil
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.StringUtil
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.shared.ui.CartIcon
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.SearchRateRequest
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.AspectRatio
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CarouselSlider
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.tenant.dto.Tenant
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.util.Locale
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/product")
class ProductScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val shippingApi: WutsiShippingApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
    private val cityService: CityService
) : AbstractQuery() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ProductScreen::class.java)
    }

    @PostMapping
    fun index(@RequestParam id: Long, request: HttpServletRequest): Widget {
        val product = catalogApi.getProduct(id).product
        val account = securityContext.currentAccount()
        val merchant = accountApi.getAccount(product.accountId).account
        val tenant = tenantProvider.get()
        val cart = getCart(merchant)

        val children = mutableListOf<WidgetAware>(
            Container(
                padding = 10.0,
                child = Text(
                    caption = StringUtil.capitalizeFirstLetter(product.title),
                    size = Theme.TEXT_SIZE_LARGE,
                    bold = true
                )
            ),
        )

        // Pictures
        if (product.pictures.isNotEmpty())
            children.add(
                CarouselSlider(
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

        // Price
        if (product.price != null)
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Container(
                        padding = 10.0,
                        child = toPriceWidget(product, tenant)
                    )
                )
            )

        // Summary
        if (!product.summary.isNullOrEmpty())
            children.add(
                Container(
                    padding = 10.0,
                    child = Text(product.summary!!)
                )
            )

        // Stock
        children.add(
            Container(
                padding = 10.0,
                child = toStockWidget(account, merchant, product, tenant)
            )
        )

        // Product Details
        if (!product.description.isNullOrEmpty())
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Container(
                        padding = 10.0,
                        child = Column(
                            mainAxisAlignment = MainAxisAlignment.start,
                            crossAxisAlignment = CrossAxisAlignment.start,
                            children = listOf(
                                Text(
                                    getText("page.product.product-details"),
                                    size = Theme.TEXT_SIZE_X_LARGE,
                                    bold = true
                                ),
                                Text(product.description!!)
                            ),
                        )
                    ),
                )
            )

        // Vendor
        val productUrl = "${tenant.webappUrl}/product?id=$$id"
        val whatsappUrl = PhoneUtil.toWhatsAppUrl(merchant.whatsapp, productUrl)
        children.add(Divider(color = Theme.COLOR_DIVIDER))
        children.add(toVendorWidget(merchant, whatsappUrl, productUrl))

        try {
            // Screen
            return Screen(
                id = Page.PRODUCT,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_WHITE,
                    foregroundColor = Theme.COLOR_BLACK,
                    actions = listOfNotNull(
                        whatsappUrl?.let {
                            Container(
                                padding = 4.0,
                                child = CircleAvatar(
                                    radius = 20.0,
                                    backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                                    child = IconButton(
                                        icon = Theme.ICON_CHAT,
                                        size = 20.0,
                                        action = Action(
                                            type = ActionType.Navigate,
                                            url = it,
                                        )
                                    )
                                ),
                            )
                        },
                        Container(
                            padding = 4.0,
                            child = CircleAvatar(
                                radius = 20.0,
                                backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                                child = IconButton(
                                    icon = Theme.ICON_SHARE,
                                    size = 20.0,
                                    action = Action(
                                        type = ActionType.Share,
                                        url = productUrl,
                                    )
                                ),
                            )
                        ),
                        cart?.let {
                            Container(
                                padding = 4.0,
                                child = CircleAvatar(
                                    radius = 20.0,
                                    backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                                    child = CartIcon(
                                        productCount = it.products.size,
                                        size = 20.0,
                                        action = gotoUrl(urlBuilder.build("cart?merchant-id=${merchant.id}"))
                                    ),
                                )
                            )
                        }
                    )
                ),
                child = Container(
                    child = ListView(
                        children = children,
                    )
                ),
                bottomNavigationBar = bottomNavigationBar()
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

    private fun toStockWidget(account: Account, merchant: Account, product: Product, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>(
            // Stock
            Row(
                children = listOf(
                    if (product.quantity > 0)
                        Icon(code = Theme.ICON_CHECK, color = Theme.COLOR_SUCCESS, size = 16.0)
                    else
                        Icon(code = Theme.ICON_CANCEL, color = Theme.COLOR_DANGER, size = 16.0),

                    Container(padding = 5.0),

                    if (product.quantity > 0)
                        Text(getText("page.product.in-stock"))
                    else
                        Text(getText("page.product.out-of-stock"), color = Theme.COLOR_DANGER)
                )
            )
        )

        if (product.quantity > 0) {
            children.addAll(
                listOf(
                    Container(padding = 10.0),
                    toShippingWidget(account, merchant, product, tenant)
                )
            )

            if (togglesProvider.isCartEnabled()) {
                children.addAll(
                    listOf(
                        Container(padding = 10.0),
                        Button(
                            padding = 10.0,
                            caption = getText("page.product.button.add-to-cart"),
                            action = executeCommand(
                                url = urlBuilder.build("commands/add-to-cart?product-id=${product.id}&merchant-id=${merchant.id}")
                            )
                        )
                    )
                )
            }
        }

        return Column(
            children = children
        )
    }

    private fun toShippingWidget(account: Account, merchant: Account, product: Product, tenant: Tenant): WidgetAware {
        // Find shipping rates
        val rates = shippingApi.searchRate(
            request = SearchRateRequest(
                cityId = account.cityId,
                country = account.country,
                accountId = merchant.id,
                products = listOf(
                    com.wutsi.ecommerce.shipping.dto.Product(
                        productId = product.id,
                        productType = product.type
                    )
                )
            )
        ).rates
        if (rates.isEmpty()) {
            val language = LocaleContextHolder.getLocale().language
            var location = Locale(language, account.country).displayCountry
            if (account.cityId != null) {
                val city = cityService.get(account.cityId)
                if (city != null)
                    location = city.name + ", " + Locale(language, city.country).displayCountry
            }

            return Row(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Icon(code = Theme.ICON_WARNING, size = 16.0, color = Theme.COLOR_DANGER),
                    Container(padding = 5.0),
                    Text(
                        caption = getText("page.product.shipping-none", arrayOf(location)),
                        color = Theme.COLOR_DANGER,
                        bold = true
                    )
                )
            )
        }

        // Show rates
        val pickup = rates.find { it.shippingType == ShippingType.LOCAL_PICKUP.name }
        val deliveries = rates.filter { it.shippingType != ShippingType.LOCAL_PICKUP.name }
            .sortedByDescending { it.rate }

        val children = mutableListOf<WidgetAware>()
        if (pickup != null)
            children.add(
                Row(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
                        Icon(code = Theme.ICON_CHECK, size = 16.0, color = Theme.COLOR_SUCCESS),
                        Container(padding = 5.0),
                        Text(getText("page.product.shipping-pickup"))
                    )
                )
            )

        if (deliveries.isNotEmpty()) {
            val delivery = deliveries[0]
            val fees = if (delivery.rate == 0.0)
                getText("label.free")
            else
                DecimalFormat(tenant.monetaryFormat).format(delivery.rate)
            children.add(
                Row(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
                        Icon(code = Theme.ICON_CHECK, size = 16.0, color = Theme.COLOR_SUCCESS),
                        Container(padding = 5.0),
                        Text(
                            getText("page.product.shipping-available") + " - " + fees
                        )
                    )
                )
            )
        }

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = children
        )
    }

    private fun toPriceWidget(product: Product, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        val price = product.price!!
        val comparablePrice = product.comparablePrice ?: 0.0
        val savings = comparablePrice - price
        val percent = (100.0 * savings / comparablePrice).toInt()
        val fmt = DecimalFormat(tenant.monetaryFormat)

        if (savings > 0) {
            children.addAll(
                listOfNotNull(
                    toRow(
                        Text(getText("page.product.list-price")),
                        Text(
                            caption = fmt.format(comparablePrice),
                            decoration = TextDecoration.Strikethrough,
                            color = Theme.COLOR_GRAY,
                            size = Theme.TEXT_SIZE_SMALL
                        )
                    ),
                    toRow(
                        Text(getText("page.product.price-with-savings")),
                        Text(
                            caption = fmt.format(price),
                            color = Theme.COLOR_PRIMARY,
                            size = Theme.TEXT_SIZE_LARGE,
                            bold = true
                        )
                    ),
                )
            )
            if (percent >= 1)
                children.add(
                    toRow(
                        Text(getText("page.product.savings")),
                        Text(
                            caption = getText("page.product.savings-percent", arrayOf(percent.toString())),
                            color = Theme.COLOR_SUCCESS,
                        )
                    )
                )
        } else {
            children.addAll(
                listOfNotNull(
                    toRow(
                        Text(getText("page.product.price")),
                        Text(
                            caption = fmt.format(price),
                            color = Theme.COLOR_PRIMARY,
                            size = Theme.TEXT_SIZE_LARGE,
                            bold = true
                        )
                    ),
                )
            )
        }

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.end,
            children = children
        )
    }

    private fun toVendorWidget(merchant: Account, whatsappUrl: String?, productUrl: String): WidgetAware =
        Container(
            child = Row(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOf(
                    Container(padding = 10.0),
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
                                merchant.category?.let { Text(it.title, color = Theme.COLOR_GRAY) },
                                whatsappUrl?.let {
                                    Button(
                                        type = ButtonType.Outlined,
                                        stretched = false,
                                        padding = 10.0,
                                        caption = getText("page.product.write-to-merchant"),
                                        action = Action(
                                            type = ActionType.Share,
                                            url = productUrl,
                                        ),
                                    )
                                }
                            )
                        )
                    )
                ),
            )
        )

    private fun toRow(name: WidgetAware, value: WidgetAware) = Row(
        mainAxisAlignment = MainAxisAlignment.start,
        crossAxisAlignment = CrossAxisAlignment.center,
        children = listOf(
            Container(child = name, padding = 2.0, width = 120.0),
            value
        ),
    )
}
