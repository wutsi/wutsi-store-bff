package com.wutsi.application.store.endpoint.cart.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.cart.WutsiCartApi
import com.wutsi.platform.cart.dto.AddProductRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/add-to-cart")
class AddToCartCommand(
    private val urlBuilder: URLBuilder,
    private val cartApi: WutsiCartApi
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "merchant-id") merchantId: Long,
        @RequestParam(name = "product-id") productId: Long,
    ): Action {
        cartApi.addProduct(
            merchantId,
            AddProductRequest(
                productId = productId,
                quantity = 1
            )
        )
        return gotoUrl(
            url = urlBuilder.build("product?id=$productId"),
            replacement = true
        )
    }
}
