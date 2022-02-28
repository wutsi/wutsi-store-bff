package com.wutsi.application.store.endpoint.settings.product.picture.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/delete-picture")
class DeletePictureCommand(
    private val catalogApi: WutsiCatalogApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam("product-id") productId: Long,
        @RequestParam("picture-id") pictureId: Long
    ): Action {
        catalogApi.deletePicture(pictureId)
        return gotoUrl(
            url = urlBuilder.build("/settings/store/product?id=$productId")
        )
    }
}
