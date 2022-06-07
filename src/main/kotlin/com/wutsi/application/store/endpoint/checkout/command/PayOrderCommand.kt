package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.CreateChargeRequest
import com.wutsi.platform.payment.dto.CreateChargeResponse
import com.wutsi.platform.payment.dto.Transaction
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
        const val DELAY_SECONDS = 15L
        const val MAX_RETRIES = 3
        private val LOGGER = LoggerFactory.getLogger(PayOrderCommand::class.java)
    }

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "payment-token") paymentToken: String
    ): Action {
        try {
            // Pay
            val order = orderApi.getOrder(orderId).order
            val response = charge(order, paymentToken)
            var status: String = response.status
            logger.add("transaction_id", response.id)

            if (response.status == Status.PENDING.name) {
                val tx = waitForCompletion(response.id)
                logger.add("transaction_status", tx.status)
                if (tx.status == Status.FAILED.name) {
                    val error = getTransactionErrorText(tx.errorCode)
                    return gotoUrl(
                        url = urlBuilder.build("/checkout/success?order-id=$orderId&error=" + encodeURLParam(error))
                    )
                }
                status = tx.status
            } else {
                logger.add("transaction_status", response.status)
            }

            if (status == Status.SUCCESSFUL.name) {
                // Open the order
                orderApi.changeStatus(orderId, ChangeStatusRequest(status = OrderStatus.OPENED.name))

                // Empty the cart
                emptyCart(order)
            }

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

    private fun charge(order: Order, paymentToken: String): CreateChargeResponse =
        paymentApi.createCharge(
            request = CreateChargeRequest(
                paymentMethodToken = paymentToken,
                recipientId = order.merchantId,
                amount = order.totalPrice,
                currency = order.currency,
                orderId = order.id
            )
        )

    private fun waitForCompletion(transactionId: String): Transaction {
        var retries = 0
        var tx = Transaction()
        try {
            while (retries++ < MAX_RETRIES) {
                LOGGER.info("$retries - Transaction #$transactionId is PENDING. Wait for $DELAY_SECONDS sec...")
                Thread.sleep(DELAY_SECONDS * 1000) // Wait for 15 secs...
                val response = paymentApi.getTransaction(transactionId)
                tx = response.transaction
                if (tx.status != Status.PENDING.name)
                    break
            }
            return tx
        } finally {
            logger.add("retries", retries - 1)
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
