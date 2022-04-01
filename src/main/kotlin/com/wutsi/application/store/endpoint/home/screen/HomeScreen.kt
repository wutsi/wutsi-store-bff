package com.wutsi.application.store.endpoint.home.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.PhoneUtil
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.application.shared.ui.CartIcon
import com.wutsi.application.shared.ui.ProductCard
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Chip
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.Wrap
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.Axis
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.tenant.dto.Tenant
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class HomeScreen(
    private val accountApi: WutsiAccountApi,
    private val catalogApi: WutsiCatalogApi,
    private val cartApi: WutsiCartApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
    private val togglesProvider: TogglesProvider,
) : AbstractQuery() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(HomeScreen::class.java)
    }

    @PostMapping
    fun index(@RequestParam(required = false) id: Long? = null): Widget {
        val children = mutableListOf<WidgetAware>()

        val merchant = id?.let { accountApi.getAccount(id).account } ?: securityContext.currentAccount()
        children.add(ProfileListItem(model = sharedUIMapper.toAccountModel(merchant)))

        val tenant = tenantProvider.get()
        children.add(toContentWidget(merchant, tenant))

        val cart = if (togglesProvider.isCartEnabled())
            getCart(merchant)
        else
            null

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
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children
                )
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }

    @PostMapping("/widget")
    fun widget(@RequestParam(required = false) id: Long? = null): Widget {
        val tenant = tenantProvider.get()
        val merchant = id?.let { accountApi.getAccount(id).account } ?: securityContext.currentAccount()

        return toContentWidget(merchant, tenant).toWidget()
    }

    private fun toContentWidget(
        merchant: Account,
        tenant: Tenant
    ): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        val sections = toSectionListWidget()
        if (sections != null) {
            children.add(Divider(color = Theme.COLOR_DIVIDER, height = 1.0))
            children.add(sections)
        }

        val products = toProductListWidget(merchant, tenant)
        children.add(Divider(color = Theme.COLOR_DIVIDER, height = 1.0))
        children.add(products)

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = children
        )
    }

    private fun toProductListWidget(
        merchant: Account,
        tenant: Tenant
    ): WidgetAware {
        val products = catalogApi.searchProducts(
            SearchProductRequest(
                limit = 100,
                accountId = merchant.id,
            )
        ).products

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = listOf(
                Container(
                    padding = 10.0,
                    child = Text(bold = true, caption = getText("page.catalog.browse-products"))
                ),
                Wrap(
                    children = products.map {
                        Container(
                            width = 180.0,
                            child = ProductCard(
                                model = sharedUIMapper.toProductModel(it, tenant),
                                action = gotoUrl(
                                    url = urlBuilder.build("/product?id=${it.id}")
                                )
                            )
                        )
                    },
                    direction = Axis.Horizontal,
                    spacing = 0.0
                ),
                Container(
                    padding = 10.0,
                    alignment = Alignment.Center,
                    child = Center(
                        child = Text(
                            alignment = TextAlignment.Center,
                            caption = getText("page.catalog.product-count", arrayOf(products.size.toString()))
                        )
                    )
                ),
            )
        )
    }

    private fun toSectionListWidget(): WidgetAware? {
        val sections = catalogApi.listSections().sections
            .filter { it.productCount > 0 }
            .sortedByDescending { it.productCount }
            .take(5)
        if (sections.isEmpty())
            return null

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = listOf(
                Container(
                    alignment = Alignment.CenterLeft,
                    padding = 10.0,
                    child = Text(bold = true, caption = getText("page.catalog.browse-by-sections"))
                ),
                Center(
                    child = Wrap(
                        spacing = 10.0,
                        direction = Axis.Horizontal,
                        children = sections.map {
                            Container(
                                child = Chip(
                                    caption = "${it.title} (${it.productCount})",
                                    backgroundColor = Theme.COLOR_PRIMARY,
                                    elevation = 5.0,
                                ),
                                action = gotoUrl(
                                    url = urlBuilder.build("section?id=${it.id}")
                                ),
                            )
                        }
                    )
                ),
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
