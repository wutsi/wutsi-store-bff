package com.wutsi.application.store.endpoint.cart.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/remove-from-cart")
class RemoveFromCartCommand(
    private val cartApi: WutsiCartApi
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "merchant-id") merchantId: Long,
        @RequestParam(name = "product-id") productId: Long,
    ): Action {
        cartApi.removeProduct(merchantId, productId)
        return gotoUrl(
            url = urlBuilder.build("cart?merchant-id=$merchantId"),
            replacement = true
        )
    }
}
