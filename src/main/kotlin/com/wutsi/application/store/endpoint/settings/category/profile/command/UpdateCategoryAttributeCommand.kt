package com.wutsi.application.store.endpoint.settings.category.profile.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.category.profile.dto.UpdateCategoryAttributeRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.catalog.WutsiCatalogApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/update-category-attribute")
class UpdateCategoryAttributeCommand(
    private val catalogApi: WutsiCatalogApi,
    private val urlBuilder: URLBuilder,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam name: String,
        @RequestBody request: UpdateCategoryAttributeRequest
    ): Action {
        catalogApi.updateCategoryAttribute(
            id = id,
            name = name,
            request = com.wutsi.platform.catalog.dto.UpdateCategoryAttributeRequest(
                value = request.value
            )
        )

        return if (name == "visible")
            gotoUrl(urlBuilder.build("/settings/store/category?id=$id"), true)
        else
            gotoPreviousScreen()
    }
}
