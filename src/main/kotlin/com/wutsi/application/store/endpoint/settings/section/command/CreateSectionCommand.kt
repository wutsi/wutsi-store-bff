package com.wutsi.application.store.endpoint.settings.section.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/create-section")
class CreateSectionCommand(private val catalogApi: WutsiCatalogApi) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestBody request: com.wutsi.application.store.endpoint.settings.section.dto.CreateSectionRequest
    ): Action {
        catalogApi.createSection(
            request = com.wutsi.ecommerce.catalog.dto.CreateSectionRequest(
                title = request.title,
            )
        )
        return gotoPreviousScreen()
    }
}
