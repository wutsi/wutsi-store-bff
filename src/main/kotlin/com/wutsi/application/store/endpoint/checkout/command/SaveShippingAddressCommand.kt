package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.checkout.dto.SaveShippingAddressRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.SetAddressRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/save-shipping-address")
class SaveShippingAddressCommand(
    private val orderApi: WutsiOrderApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "country") country: String,
        @RequestBody request: SaveShippingAddressRequest
    ): Action {
        orderApi.setShippingAddress(
            orderId,
            SetAddressRequest(
                firstName = request.firstName,
                lastName = request.lastName,
                country = country,
                cityId = request.cityId,
                email = request.email,
                street = request.street
            )
        )

        return gotoUrl(
            urlBuilder.build("checkout/shipping?order-id=$orderId")
        )
    }
}
