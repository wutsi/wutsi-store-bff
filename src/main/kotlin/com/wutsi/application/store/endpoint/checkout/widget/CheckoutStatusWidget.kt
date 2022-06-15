package com.wutsi.application.store.endpoint.checkout.widget

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Noop
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.Status
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
    }

    @PostMapping
    fun index(
        @RequestParam(name = "transaction-id") transactionId: String,
        @RequestParam(name = "count", required = false) count: Int? = null
    ): Widget {
        try {
            val tx = paymentApi.getTransaction(transactionId).transaction
            logger.add("transaction_status", tx.status)

            if (tx.status == Status.PENDING.name)
                return Noop().toWidget()

            // Success widget
            return Column(
                children = listOf(
                    Icon(code = Theme.ICON_CHECK, size = 80.0, color = Theme.COLOR_SUCCESS),
                    Container(
                        padding = 10.0,
                        child = Text("widget.processing.success")
                    )
                )
            ).toWidget()
        } catch (ex: FeignException.Conflict) {
            // Error widget
            val error = getErrorText(ex)
            return Column(
                children = listOf(
                    Icon(code = Theme.ICON_ERROR, size = 80.0, color = Theme.COLOR_DANGER),
                    Container(
                        padding = 10.0,
                        child = Text(error)
                    )
                )
            ).toWidget()
        } catch (ex: Throwable) {
            LOGGER.warn("Unexpected error", ex)
            return Noop().toWidget()
        }
    }
}
