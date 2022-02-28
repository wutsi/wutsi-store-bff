package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/update-product-attribute")
class UpdateProductAttributeCommand(
    private val catalogApi: WutsiCatalogApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam name: String,
        @RequestBody request: UpdateProductAttributeRequest
    ): Action {
        catalogApi.updateProductAttribute(
            id = id,
            name = name,
            request = com.wutsi.ecommerce.catalog.dto.UpdateProductAttributeRequest(
                value = request.value
            )
        )

        return if (name == "visible")
            gotoUrl(urlBuilder.build("/settings/store/product?id=$id"), true)
        else
            gotoPreviousScreen()
    }
}
