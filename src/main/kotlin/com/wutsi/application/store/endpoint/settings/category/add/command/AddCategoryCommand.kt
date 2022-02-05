package com.wutsi.application.store.endpoint.settings.category.add.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.category.add.dto.AddCategoryRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.CreateCategoryRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/add-category")
class AddCategoryCommand(
    val urlBuilder: URLBuilder,
    val catalogApi: WutsiCatalogApi
) : AbstractCommand() {
    @PostMapping
    fun index(@RequestBody request: AddCategoryRequest): Action {
        catalogApi.createCategory(
            request = CreateCategoryRequest(
                title = request.title,
            )
        ).id

        return gotoPreviousScreen()
    }
}
