package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/submit-order")
class SubmitOrderCommand(
    private val orderApi: WutsiOrderApi,
    private val logger: KVLogger,
) : AbstractCommand() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(SubmitOrderCommand::class.java)
    }

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
    ): Action {
        try {
            // Open the order
            orderApi.changeStatus(orderId, ChangeStatusRequest(status = OrderStatus.OPENED.name))

            // Empty the cart
            val order = orderApi.getOrder(orderId).order
            emptyCart(order)

            return gotoUrl(
                url = urlBuilder.build("checkout/success?order-id=$orderId")
            )
        } catch (ex: FeignException) {
            logger.setException(ex)
            val error = getErrorText(ex)
            return gotoUrl(
                url = urlBuilder.build("checkout/success?order-id=$orderId&error=" + encodeURLParam(error))
            )
        }
    }

    private fun emptyCart(order: Order) {
        try {
            cartApi.emptyCart(order.merchantId)
        } catch (ex: Exception) {
            LOGGER.warn("Unable to empty the cart")
        }
    }
}
