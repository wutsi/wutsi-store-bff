package com.wutsi.application.store.endpoint.settings.product.add.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.product.add.dto.AddProductRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.CreateProductRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/add-product")
class AddProductCommand(
    val urlBuilder: URLBuilder,
    val catalogApi: WutsiCatalogApi
) : AbstractCommand() {
    @PostMapping
    fun index(@RequestBody request: AddProductRequest): Action {
        val id = catalogApi.createProduct(
            request = CreateProductRequest(
                title = request.title,
                summary = request.summary,
                price = if (request.price == 0.0) null else request.price
            )
        ).id

        return gotoUrl(
            url = urlBuilder.build("settings/store/product?id=$id"),
            replacement = true
        )
    }
}
