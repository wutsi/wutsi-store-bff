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
        val MAX_COUNT = 3
    }

    @PostMapping
    fun index(
        @RequestParam(name = "transaction-id") transactionId: String,
        @RequestParam(name = "count") count: Int
    ): Widget {
        try {
            val tx = paymentApi.getTransaction(transactionId).transaction
            logger.add("transaction_status", tx.status)

            if (tx.status == Status.PENDING.name) {
                return if (count > MAX_COUNT)
                    Column(
                        children = listOf(
                            Icon(code = Theme.ICON_PENDING, size = 40.0, color = Theme.COLOR_PRIMARY),
                            Container(
                                padding = 10.0,
                                child = Text(getText("widget.checkout.processing.pending"))
                            )
                        )
                    ).toWidget()
                else
                    Noop().toWidget()
            }

            // Success widget
            return Column(
                children = listOf(
                    Icon(code = Theme.ICON_CHECK, size = 40.0, color = Theme.COLOR_SUCCESS),
                    Container(
                        padding = 10.0,
                        child = Text(getText("widget.checkout.processing.success"))
                    )
                )
            ).toWidget()
        } catch (ex: FeignException.Conflict) {
            // Error widget
            val error = getErrorText(ex)
            return Column(
                children = listOf(
                    Icon(code = Theme.ICON_ERROR, size = 40.0, color = Theme.COLOR_DANGER),
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
