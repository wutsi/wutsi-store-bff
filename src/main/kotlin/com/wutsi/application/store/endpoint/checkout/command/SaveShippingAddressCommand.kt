package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.checkout.dto.SaveShippingAddressRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.CreateAddressRequest
import com.wutsi.ecommerce.order.dto.SetAddressRequest
import com.wutsi.ecommerce.order.entity.AddressType
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
        @RequestParam(required = false) country: String? = null,
        @RequestParam type: AddressType,
        @RequestBody request: SaveShippingAddressRequest
    ): Action {
        val address = orderApi.createAddress(
            CreateAddressRequest(
                firstName = request.firstName,
                lastName = request.lastName,
                country = country,
                cityId = request.cityId,
                email = request.email,
                street = request.street,
                type = type.name
            )
        )
        orderApi.setShippingAddress(
            orderId,
            SetAddressRequest(addressId = address.id)
        )

        return gotoUrl(
            urlBuilder.build("checkout/shipping?order-id=$orderId")
        )
    }
}
