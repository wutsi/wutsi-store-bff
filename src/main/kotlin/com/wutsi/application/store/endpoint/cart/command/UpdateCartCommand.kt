package com.wutsi.application.store.endpoint.cart.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.cart.dto.UpdateCartRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.cart.WutsiCartApi
import com.wutsi.platform.cart.dto.UpdateProductRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/update-cart")
class UpdateCartCommand(
    private val urlBuilder: URLBuilder,
    private val cartApi: WutsiCartApi
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "merchant-id") merchantId: Long,
        @RequestParam(name = "product-id") productId: Long,
        @RequestBody request: UpdateCartRequest
    ): Action {
        cartApi.updateProduct(merchantId, productId, UpdateProductRequest(request.quantity))
        return gotoUrl(
            url = urlBuilder.build("cart?merchant-id=$merchantId"),
            replacement = true
        )
    }
}
