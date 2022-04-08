package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.error.ErrorResponse
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/publish-product")
class PublishProductCommand(
    private val catalogApi: WutsiCatalogApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestBody request: UpdateProductAttributeRequest
    ): Action {
        try {
            if (request.value == "true")
                catalogApi.publishProduct(id)
            else
                catalogApi.unpublishProduct(id)

            return gotoUrl(urlBuilder.build("/settings/store/product?id=$id"), true)
        } catch (ex: FeignException) {
            val errors = extractPublishingErrors(ex)
            return gotoUrl(urlBuilder.build("/settings/store/product?id=$id&errors=$errors"), true)
        }
    }

    private fun extractPublishingErrors(ex: FeignException): String {
        val response = ObjectMapper().readValue(ex.contentUTF8(), ErrorResponse::class.java)
        val errors = response.error.data?.get("publishing-errors")
        return if (errors is List<*>)
            errors
                .map { "error.product.publish.$it" }
                .joinToString(separator = ",")
        else
            errors.toString()
    }
}
