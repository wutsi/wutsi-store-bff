package com.wutsi.application.store.endpoint.catalog.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.PhoneUtil
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.CartIcon
import com.wutsi.application.shared.ui.ProductCard
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.cart.WutsiCartApi
import com.wutsi.platform.cart.dto.Cart
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.ProductSummary
import com.wutsi.platform.catalog.dto.SearchProductRequest
import com.wutsi.platform.tenant.dto.Tenant
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog")
class CatalogScreen(
    private val urlBuilder: URLBuilder,
    private val accountApi: WutsiAccountApi,
    private val catalogApi: WutsiCatalogApi,
    private val cartApi: WutsiCartApi,
    private val tenantProvider: TenantProvider,
    private val securityContext: SecurityContext,
    private val sharedUIMapper: SharedUIMapper,
    private val togglesProvider: TogglesProvider,
) : AbstractQuery() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(CatalogScreen::class.java)
    }

    @PostMapping
    fun index(@RequestParam(required = false) id: Long? = null): Widget {
        val merchant = id?.let { accountApi.getAccount(id).account } ?: securityContext.currentAccount()
        val tenant = tenantProvider.get()
        val cart = if (togglesProvider.isCartEnabled())
            getCart(merchant)
        else
            null
        val products = catalogApi.searchProducts(
            SearchProductRequest(
                accountId = merchant.id,
                limit = 100
            )
        ).products
        val rows = toRows(products, 2)

        val profileUrl = "${tenant.webappUrl}/profile?id=$${merchant.id}"
        val whatsappUrl = PhoneUtil.toWhatsAppUrl(merchant.whatsapp, profileUrl)
        return Screen(
            id = Page.CATALOG,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.catalog.app-bar.title"),
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
                                    url = profileUrl,
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
                ),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    ProfileListItem(model = sharedUIMapper.toAccountModel(merchant)),
                    Flexible(
                        child = ListView(
                            children = rows.map {
                                Container(
                                    child = Row(
                                        children = thumbnails(it, tenant),
                                    )
                                )
                            }
                        ),
                    ),
                )
            )
        ).toWidget()
    }

    private fun toRows(products: List<ProductSummary>, size: Int): List<List<ProductSummary>> {
        val rows = mutableListOf<List<ProductSummary>>()
        var cur = mutableListOf<ProductSummary>()
        products.forEach {
            cur.add(it)
            if (cur.size == size) {
                rows.add(cur)
                cur = mutableListOf()
            }
        }
        if (cur.isNotEmpty())
            rows.add(cur)
        return rows
    }

    private fun thumbnails(products: List<ProductSummary>, tenant: Tenant): List<WidgetAware> =
        products.map {
            Flexible(
                child = Container(
                    alignment = Alignment.TopCenter,
                    child = ProductCard(
                        model = sharedUIMapper.toProductModel(it, tenant),
                        action = gotoUrl(urlBuilder.build("/product?id=${it.id}"))
                    )
                )
            )
        }

    private fun getCart(merchant: Account): Cart? =
        try {
            cartApi.getCart(merchant.id).cart
        } catch (ex: Exception) {
            LOGGER.warn("Unable to resolve the Cart for Merchant #${merchant.id}", ex)
            null
        }
}
