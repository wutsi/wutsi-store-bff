package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.catalog.dto.SearchCategoryRequest
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.WidgetAware
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/sub-category-id")
class SettingsProductSubCategoryScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi,
) : AbstractSettingsProductAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "sub-category-id"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_SUB_CATEGORY

    override fun getInputWidget(product: Product): WidgetAware {
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                parentId = product.category.id
            )
        ).categories.sortedBy { it.title }

        return DropdownButton(
            name = "value",
            value = product.subCategory.id.toString(),
            required = true,
            children = categories
                .sortedBy { it.title }
                .map {
                    DropdownMenuItem(
                        caption = it.title,
                        value = it.id.toString(),
                    )
                }
        )
    }
}
