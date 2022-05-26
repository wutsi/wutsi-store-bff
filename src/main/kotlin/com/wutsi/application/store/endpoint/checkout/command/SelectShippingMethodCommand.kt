package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.service.ShippingService
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
    private val service: ShippingService,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "shipping-id") shippingId: Long,
    ): Action {
        val account = securityContext.currentAccount()
        val order = orderApi.getOrder(orderId).order
        val rate = service.findShippingRate(shippingId, account, order)

        orderApi.setShippingMethod(
            orderId,
            SetShippingMethodRequest(
                shippingId = shippingId,
                deliveryTime = rate?.deliveryTime,
                deliveryFees = rate?.rate ?: 0.0
            )
        )

        return gotoUrl(
            urlBuilder.build("/checkout/review?order-id=$orderId")
        )
    }
}
