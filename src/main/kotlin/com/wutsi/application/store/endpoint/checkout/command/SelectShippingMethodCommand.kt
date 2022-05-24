package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.SetShippingMethodRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/select-shipping-method")
class SelectShippingMethodCommand(
    private val orderApi: WutsiOrderApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "shipping-id") shippingId: Long,
    ): Action {
        orderApi.setShippingMethod(
            orderId,
            SetShippingMethodRequest(
                shippingId = shippingId,
            )
        )

        return gotoUrl(
            urlBuilder.build("/checkout/review?order-id=$orderId")
        )
    }
}
