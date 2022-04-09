package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.dto.CreateTransferRequest
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/pay-order")
class PayOrderCommand(
    private val orderApi: WutsiOrderApi,
    private val paymentApi: WutsiPaymentApi,
    private val logger: KVLogger,
) : AbstractCommand() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PayOrderCommand::class.java)
    }

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
    ): Action {
        try {
            // Pay
            val order = orderApi.getOrder(orderId).order
            val id = paymentApi.createTransfer(
                request = CreateTransferRequest(
                    recipientId = order.merchantId,
                    amount = order.totalPrice,
                    currency = order.currency,
                    orderId = order.id
                )
            ).id
            logger.add("transaction_id", id)

            // Empty the cart
            emptyCart(order)

            return gotoUrl(
                url = urlBuilder.build("/checkout/success?order-id=$orderId")
            )
        } catch (ex: FeignException) {
            logger.setException(ex)
            val error = getErrorText(ex)
            return gotoUrl(
                url = urlBuilder.build("/checkout/success?order-id=$orderId&error=" + encodeURLParam(error))
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
