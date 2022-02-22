package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.dto.CreateTransferRequest
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/pay-order")
class PayOrderCommand(
    private val urlBuilder: URLBuilder,
    private val orderApi: WutsiOrderApi,
    private val paymentApi: WutsiPaymentApi,
    private val logger: KVLogger,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
    ): Action {
        try {
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
            return gotoUrl(
                url = urlBuilder.build("/checkout/success?order-id=$orderId")
            )
        } catch (ex: FeignException.Conflict) {
            logger.setException(ex)
            val error = getErrorText(ex)
            return gotoUrl(
                url = urlBuilder.build("/checkout/success?order-id=$orderId&error=" + encodeURLParam(error))
            )
        }
    }
}
