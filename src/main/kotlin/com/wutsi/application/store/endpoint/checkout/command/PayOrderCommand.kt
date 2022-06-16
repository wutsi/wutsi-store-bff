package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.CreateChargeRequest
import com.wutsi.platform.payment.dto.CreateChargeResponse
import feign.FeignException
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
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "payment-token") paymentToken: String,
        @RequestParam(name = "idempotency-key") idempotencyKey: String
    ): Action {
        logger.add("order_id", orderId)
        logger.add("payment_token", paymentToken)
        logger.add("idempotency_key", idempotencyKey)

        try {
            // Pay
            val order = orderApi.getOrder(orderId).order
            val response = charge(order, paymentToken, idempotencyKey)
            logger.add("transaction_id", response.id)
            logger.add("transaction_status", response.status)

            // Next page
            return if (response.status == Status.SUCCESSFUL.name)
                return gotoUrl(
                    url = urlBuilder.build("/checkout/success?order-id=$orderId")
                )
            else
                gotoUrl(
                    url = urlBuilder.build("/checkout/processing?order-id=$orderId&transaction-id=${response.id}")
                )
        } catch (ex: FeignException) {
            logger.setException(ex)
            val error = getErrorText(ex)
            return gotoUrl(
                url = urlBuilder.build("/checkout/success?order-id=$orderId&error=" + encodeURLParam(error))
            )
        }
    }

    private fun charge(order: Order, paymentToken: String, idempotencyKey: String): CreateChargeResponse =
        paymentApi.createCharge(
            request = CreateChargeRequest(
                paymentMethodToken = if (paymentToken == "WALLET") null else paymentToken,
                recipientId = order.merchantId,
                amount = order.totalPrice,
                currency = order.currency,
                orderId = order.id,
                idempotencyKey = idempotencyKey
            )
        )
}
