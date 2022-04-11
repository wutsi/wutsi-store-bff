package com.wutsi.application.store.endpoint.marketplace

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.model.AccountModel
import com.wutsi.application.shared.model.ProductModel
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.ProductActionProvider
import com.wutsi.application.shared.ui.ProductGridView
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.catalog.entity.ProductStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Widget
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.SearchAccountRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/marketplace/category")
class MarketplaceCategoryScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val sharedUIMapper: SharedUIMapper,
    private val tenantProvider: TenantProvider,
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
    fun index(@RequestParam id: Long): Widget {
        val tenant = tenantProvider.get()
        val category = catalogApi.getCategory(id).category
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                categoryIds = listOf(id),
                status = ProductStatus.PUBLISHED.name,
                limit = 200
            )
        ).products

        val accountIds = products.map { it.accountId }.toSet()
        val merchants = accountApi.searchAccount(
            request = SearchAccountRequest(
                ids = accountIds.toList(),
                limit = accountIds.size
            )
        ).accounts.associateBy { it.id }

        return Screen(
            id = Page.MARKETPLACE_CATEGORY,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = category.title,
            ),
            bottomNavigationBar = bottomNavigationBar(),
            child = SingleChildScrollView(
                child = ProductGridView(
                    spacing = 5.0,
                    productsPerRow = 2,
                    models = products.map { sharedUIMapper.toProductModel(it, tenant, merchants[it.accountId]) },
                    actionProvider = this,
                )
            ),
        ).toWidget()
    }
}
