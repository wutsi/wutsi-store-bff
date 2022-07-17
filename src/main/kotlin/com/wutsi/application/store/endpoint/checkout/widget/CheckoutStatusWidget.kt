package com.wutsi.application.store.endpoint.checkout.widget

import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.flutter.sdui.Noop
import com.wutsi.flutter.sdui.Widget
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.Transaction
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/widgets/checkout-status")
class CheckoutStatusWidget(
    private val paymentApi: WutsiPaymentApi,
    private val logger: KVLogger,
) : AbstractQuery() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(CheckoutStatusWidget::class.java)
        const val MAX_COUNT = 3
    }

    @PostMapping
    fun index(
        @RequestParam(name = "transaction-id") transactionId: String,
        @RequestParam(name = "count") count: Int
    ): Widget {
        try {
            val tx = paymentApi.getTransaction(transactionId).transaction
            logger.add("transaction_status", tx.status)
            return if (tx.status == Status.SUCCESSFUL.name) {
                emptyCart(tx)
                toTransactionStatusWidget(tx).toWidget()
            } else if (tx.status == Status.PENDING.name && count > MAX_COUNT)
                toTransactionStatusWidget(tx).toWidget()
            else
                Noop().toWidget()
        } catch (ex: FeignException.Conflict) {
            return toTransactionStatusWidget(null, getErrorText(ex)).toWidget()
        } catch (ex: Throwable) {
            LOGGER.warn("Unexpected error", ex)
            return Noop().toWidget()
        }
    }

    private fun emptyCart(tx: Transaction) {
        try {
            if (tx.recipientId != null && tx.orderId != null)
                tx.recipientId?.let { cartApi.emptyCart(it) }
        } catch (ex: Exception) {
            LOGGER.warn("Unable to empty the cart", ex)
        }
    }
}
