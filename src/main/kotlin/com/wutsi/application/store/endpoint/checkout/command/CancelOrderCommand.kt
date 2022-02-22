package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/cancel-order")
class CancelOrderCommand(
    private val orderApi: WutsiOrderApi,
    private val logger: KVLogger,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
    ): Action {
        return try {
            orderApi.cancelOrder(orderId)
            gotoPreviousScreen()
        } catch (ex: FeignException.Conflict) {
            logger.setException(ex)
            return showError(getErrorText(ex))
        }
    }
}
