package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.SetAddressRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/select-shipping-address")
class SelectShippingAddressCommand(
    private val orderApi: WutsiOrderApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "address-id") addressId: Long,
    ): Action {
        orderApi.setShippingAddress(
            orderId,
            SetAddressRequest(
                id = addressId
            )
        )

        return gotoUrl(
            urlBuilder.build("/checkout/shipping?order-id=$orderId")
        )
    }
}
