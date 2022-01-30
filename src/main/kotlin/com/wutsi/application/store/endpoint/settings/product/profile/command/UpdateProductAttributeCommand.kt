package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProfileAttributeRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.UpdateProductAttributeRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/update-product-attribute")
class UpdateProductAttributeCommand(
    private val catalogApi: WutsiCatalogApi,
    private val urlBuilder: URLBuilder,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam name: String,
        @RequestBody request: UpdateProfileAttributeRequest
    ): Action {
        catalogApi.updateProductAttribute(
            id = id,
            name = name,
            request = UpdateProductAttributeRequest(
                value = request.value
            )
        )

        return if (name == "visible")
            gotoUrl(urlBuilder.build("/settings/store/product?id=$id"), true)
        else
            gotoPreviousScreen()
    }
}