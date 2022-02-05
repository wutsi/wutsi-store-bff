package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.AddCategoryRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/add-product-category")
class AddProductCategoryCommand(
    private val catalogApi: WutsiCatalogApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "product-id") productId: Long,
        @RequestParam(name = "category-id") categoryId: Long,
        @RequestBody request: UpdateProductAttributeRequest
    ) {
        if (request.value == "true")
            catalogApi.addCategory(productId, AddCategoryRequest(categoryId))
        else
            catalogApi.removeCategory(productId, categoryId)
    }
}
