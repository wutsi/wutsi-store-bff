package com.wutsi.application.store.endpoint.home.screen

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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/section")
class SectionScreen(
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
) : ProductActionProvider, AbstractQuery() {
    override fun getAction(product: ProductModel): Action =
        gotoUrl(
            url = urlBuilder.build("/product?id=${product.id}")
        )

    override fun getAction(model: AccountModel): Action? =
        gotoUrl(
            url = urlBuilder.build(shellUrl, "/profile?id=${model.id}")
        )

    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val section = catalogApi.getSection(id).section
        val tenant = tenantProvider.get()
        val products = catalogApi.searchProducts(
            SearchProductRequest(
                limit = 100,
                sectionId = section.id,
                status = ProductStatus.PUBLISHED.name
            )
        ).products

        return Screen(
            id = Page.SECTION,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = section.title,
            ),
            child = SingleChildScrollView(
                child = ProductGridView(
                    spacing = 5.0,
                    productsPerRow = 2,
                    models = products.map { sharedUIMapper.toProductModel(it, tenant) },
                    actionProvider = this,
                )
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }
}
