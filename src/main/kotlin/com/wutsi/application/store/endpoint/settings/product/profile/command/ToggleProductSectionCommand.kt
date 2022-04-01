package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/toggle-product-section")
class ToggleProductSectionCommand(
    private val catalogApi: WutsiCatalogApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam(name = "section-id") sectionId: Long,
        @RequestBody request: UpdateProductAttributeRequest
    ) {
        if (request.value == "true")
            catalogApi.addToSection(
                id = sectionId,
                productId = id
            )
        else
            catalogApi.removeFromSection(
                id = sectionId,
                productId = id
            )
    }
}
