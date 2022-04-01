package com.wutsi.application.store.endpoint.settings.section.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.section.dto.UpdateSectionRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/update-section")
class UpdateSectionCommand(private val catalogApi: WutsiCatalogApi) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam(name = "sort-order") sortOrder: Int,
        @RequestBody request: UpdateSectionRequest
    ): Action {
        catalogApi.updateSection(
            id,
            request = com.wutsi.ecommerce.catalog.dto.UpdateSectionRequest(
                title = request.title,
                sortOrder = sortOrder
            )
        )
        return gotoPreviousScreen()
    }
}
