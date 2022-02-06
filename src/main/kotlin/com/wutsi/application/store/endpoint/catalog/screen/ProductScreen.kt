package com.wutsi.application.store.endpoint.catalog.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.PhoneUtil
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.AspectRatio
import com.wutsi.flutter.sdui.CarouselSlider
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.Product
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/product")
class ProductScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val product = catalogApi.getProduct(id).product
        val merchant = accountApi.getAccount(product.accountId).account
        val tenant = tenantProvider.get()

        val children = mutableListOf<WidgetAware>(
            Container(
                padding = 10.0,
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
                        // Title
                        Text(caption = product.title, size = Theme.TEXT_SIZE_LARGE, bold = true),

                        // Link to merchant store
                        Container(padding = 5.0),
                        Container(
                            action = gotoUrl(urlBuilder.build("catalog?id=${merchant.id}")),
                            child = Text(
                                caption = getText("page.product.visit-store", arrayOf(merchant.displayName)),
                                color = Theme.COLOR_PRIMARY,
                                decoration = TextDecoration.Underline,
                                size = Theme.TEXT_SIZE_SMALL
                            )
                        ),
                    )
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
                    height = 300.0,
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
                        child = priceWidget(product, tenant)
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

        // Product Details
        if (!product.description.isNullOrEmpty()) {
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
        }

        val productUrl = "${tenant.webappUrl}/product?id=${product.id}"
        return Screen(
            id = Page.PRODUCT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                actions = listOfNotNull(
                    PhoneUtil.toWhatsAppUrl(merchant.whatsapp, productUrl)?.let {
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
                                    message = product.title,
                                    url = productUrl,
                                )
                            ),
                        )
                    ),
                )
            ),
            child = Container(
                child = ListView(
                    children = children,
                )
            )
        ).toWidget()
    }

    private fun priceWidget(product: Product, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        val price = product.price!!
        val comparablePrice = product.comparablePrice ?: 0.0
        val savings = comparablePrice - price
        val percent = (100.0 * savings / comparablePrice).toInt()
        val fmt = DecimalFormat(tenant.monetaryFormat)

        if (savings > 0) {
            children.addAll(
                listOfNotNull(
                    row(
                        Text(getText("page.product.list-price")),
                        Text(
                            caption = fmt.format(comparablePrice),
                            decoration = TextDecoration.Strikethrough,
                            color = Theme.COLOR_GRAY,
                            size = Theme.TEXT_SIZE_SMALL
                        )
                    ),
                    row(
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
                    row(
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
                    row(
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

    private fun row(name: WidgetAware, value: WidgetAware) = Row(
        mainAxisAlignment = MainAxisAlignment.start,
        crossAxisAlignment = CrossAxisAlignment.center,
        children = listOf(
            Container(child = name, padding = 2.0, width = 120.0),
            value
        ),
    )
}
