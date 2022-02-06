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
import org.springframework.beans.factory.annotation.Value
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

    @Value("\${wutsi.store.product.deep-link}") private val productDeepLink: String,
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
                        // Link to merchant store
                        Container(
                            action = gotoUrl(urlBuilder.build("catalog?id=${merchant.id}")),
                            child = Text(
                                caption = getText("page.product.visit-store", arrayOf(merchant.displayName)),
                                color = Theme.COLOR_PRIMARY,
                                decoration = TextDecoration.Underline
                            )
                        ),

                        // Title
                        Container(padding = 5.0),
                        Text(caption = product.title, size = Theme.TEXT_SIZE_LARGE)
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

        // Price + Summary
        if (product.price != null || !product.summary.isNullOrEmpty())
            children.addAll(
                listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Container(
                        padding = 10.0,
                        child = Column(
                            mainAxisAlignment = MainAxisAlignment.start,
                            crossAxisAlignment = CrossAxisAlignment.start,
                            children = listOfNotNull(
                                priceWidget(product, tenant),
                                Container(padding = 5.0),

                                if (product.summary.isNullOrEmpty())
                                    null
                                else
                                    product.summary?.let { Text(it) }
                            )
                        )
                    )
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

        val productUrl = "$productDeepLink?id=${product.id}"
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

    private fun priceWidget(product: Product, tenant: Tenant): WidgetAware? =
        if (product.price != null)
            Row(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.end,
                children = listOfNotNull(
                    Text(
                        caption = DecimalFormat(tenant.monetaryFormat).format(product.price),
                        size = Theme.TEXT_SIZE_X_LARGE,
                        bold = true,
                        color = Theme.COLOR_PRIMARY
                    ),
                    if (product.comparablePrice != null && product.price != null && product.comparablePrice!! > product.price!!)
                        Container(
                            child = Text(
                                caption = DecimalFormat(tenant.monetaryFormat).format(product.comparablePrice),
                                decoration = TextDecoration.Strikethrough,
                                color = Theme.COLOR_GRAY
                            )
                        )
                    else
                        null,
                ),
            )
        else
            null
}
