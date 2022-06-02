package com.wutsi.application.store.endpoint.marketplace

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.cart.dto.CartSummary
import com.wutsi.ecommerce.cart.dto.SearchCartRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchMerchantRequest
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Image
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
import com.wutsi.platform.account.dto.Category
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.account.entity.AccountSort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/marketplace")
class MarketplaceScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,

    @Value("\${wutsi.application.shell-url}") shellUrl: String,
) : AbstractQuery() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(MarketplaceScreen::class.java)
    }

    @PostMapping
    fun index(): Widget {
        val children = mutableListOf<WidgetAware>()

        // Get merchants
        val merchants = catalogApi.searchMerchants(
            request = SearchMerchantRequest(
                withPublishedProducts = true,
                limit = 30,
            )
        ).merchants

        // Get stores
        val stores = if (merchants.isEmpty())
            emptyList()
        else
            accountApi.searchAccount(
                request = SearchAccountRequest(
                    ids = merchants.map { it.accountId },
                    sortBy = AccountSort.NAME.name
                )
            ).accounts

        if (stores.isNotEmpty()) {
            // Categories
            val categories = accountApi.listCategories().categories

            // Opened Carts
            val carts = getOpenedCarts()
            val cartWidget = toCartListWidget(carts, stores, categories)
            if (cartWidget != null)
                children.add(cartWidget)

            // Stores
            if (children.isNotEmpty()) {
                children.add(Container(padding = 10.0))
            }
            children.add(toStoreListWidget(stores, categories))
        }

        return Screen(
            id = Page.MARKETPLACE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.marketplace.app-bar.title"),
            ),
            bottomNavigationBar = bottomNavigationBar(),
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            child = SingleChildScrollView(
                child = if (stores.isEmpty())
                    Center(
                        Container(
                            padding = 10.0,
                            child = Text(getText("page.marketplace.empty"))
                        )
                    )
                else {
                    Column(
                        mainAxisAlignment = MainAxisAlignment.start,
                        crossAxisAlignment = CrossAxisAlignment.start,
                        children = children
                    )
                }
            ),
        ).toWidget()
    }

    private fun toStoreListWidget(stores: List<AccountSummary>, categories: List<Category>): WidgetAware {
        val categoryMap = categories.associateBy { it.id }
        val children = mutableListOf<WidgetAware>()
        children.add(
            Center(
                Container(
                    padding = 10.0,
                    child = Text(getText("page.marketplace.stores"), bold = true)
                )
            ),
        )
        children.addAll(
            stores.flatMap {
                listOf(
                    Divider(height = 1.0, color = Theme.COLOR_DIVIDER),
                    ProfileListItem(
                        model = sharedUIMapper.toAccountModel(
                            it,
                            category = if (it.categoryId == null) null else categoryMap[it.categoryId],
                        ),
                        action = gotoUrl(urlBuilder.build(shellUrl, "/profile?id=${it.id}&tab=store")),
                        showAccountType = false,
                        showLocation = true
                    ),
                )
            }
        )
        return toSectionWidget(
            padding = null,
            child = Column(
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisAlignment = MainAxisAlignment.start,
                children = children
            ),
        )
    }

    private fun toCartListWidget(
        carts: List<CartSummary>,
        stores: List<AccountSummary>,
        categories: List<Category>
    ): WidgetAware? {
        if (carts.isEmpty())
            return null

        val categoryMap = categories.associateBy { it.id }
        val storeMap = stores.associateBy { it.id }.toMutableMap()
        val children = mutableListOf<WidgetAware>()
        carts.forEach {
            val cart = toCartWidget(it, storeMap, categoryMap)
            if (cart != null) {
                children.add(Divider(height = 1.0, color = Theme.COLOR_DIVIDER))
                children.add(cart)
            }
        }
        if (children.isEmpty())
            return null

        children.add(
            0, Center(
                Container(
                    padding = 10.0,
                    child = Text(getText("page.marketplace.carts"), bold = true)
                )
            )
        )
        return toSectionWidget(
            padding = null,
            child = Column(
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisAlignment = MainAxisAlignment.start,
                children = children
            ),
        )
    }

    private fun toCartWidget(
        cart: CartSummary,
        storeMap: MutableMap<Long, AccountSummary>,
        categoryMap: Map<Long, Category>
    ): WidgetAware? {
        try {
            val store = getStore(cart, storeMap) ?: return null
            val cart = cartApi.getCart(store.id).cart
            val products = catalogApi.searchProducts(
                request = SearchProductRequest(
                    productIds = cart.products.map { it.productId },
                    limit = 7
                )
            ).products
            if (products.isEmpty())
                return null

            return Container(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOfNotNull(
                        ProfileListItem(
                            model = sharedUIMapper.toAccountModel(
                                store,
                                category = if (store.categoryId == null) null else categoryMap[store.categoryId],
                            ),
                            action = gotoUrl(urlBuilder.build(shellUrl, "/profile?id=${store.id}&tab=store")),
                            showAccountType = false,
                            showLocation = true
                        ),
                        Row(
                            mainAxisAlignment = MainAxisAlignment.start,
                            crossAxisAlignment = CrossAxisAlignment.start,
                            children = products.map {
                                Container(
                                    padding = 5.0,
                                    child = Image(
                                        url = it.thumbnail?.url ?: "",
                                        width = 24.0,
                                        height = 24.0
                                    )
                                )
                            }
                        )
                    )
                )
            )

        } catch (ex: Exception) {
            LOGGER.warn("Unexpected error", ex)
            return null
        }
    }

    private fun getOpenedCarts(): List<CartSummary> =
        try {
            if (togglesProvider.isCartEnabled())
                cartApi.searchCarts(
                    request = SearchCartRequest(
                        accountId = securityContext.currentAccountId(),
                        limit = 3
                    )
                ).carts
            else
                emptyList()
        } catch (ex: Exception) {
            LOGGER.warn("Unable to resolve the opened carts", ex)
            emptyList<CartSummary>()
        }

    private fun getStore(cart: CartSummary, storeMap: MutableMap<Long, AccountSummary>): AccountSummary? {
        val merchant = storeMap[cart.merchantId]
        return merchant
            ?: loadStore(cart.merchantId, storeMap)
    }

    private fun loadStore(merchantId: Long, merchantMap: MutableMap<Long, AccountSummary>): AccountSummary? {
        val merchants = accountApi.searchAccount(
            SearchAccountRequest(ids = listOf(merchantId))
        ).accounts

        if (merchants.isNotEmpty()) {
            merchantMap[merchantId] = merchants[0]
            return merchants[0]
        }
        return null
    }
}
