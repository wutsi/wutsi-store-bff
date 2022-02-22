package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.CreateOrderItem
import com.wutsi.ecommerce.order.dto.CreateOrderRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/create-order")
class CreateOrderCommand(
    private val urlBuilder: URLBuilder,
    private val cartApi: WutsiCartApi,
    private val orderApi: WutsiOrderApi,
    private val logger: KVLogger,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "merchant-id") merchantId: Long,
    ): Action {
        try {
            val cart = cartApi.getCart(merchantId).cart
            val orderId = orderApi.createOrder(
                CreateOrderRequest(
                    merchantId = merchantId,
                    items = cart.products.map {
                        CreateOrderItem(
                            productId = it.productId,
                            quantity = it.quantity
                        )
                    }
                )
            ).id
            logger.add("order_id", orderId)
            return gotoUrl(
                url = urlBuilder.build("checkout/review?order-id=$orderId")
            )
        } catch (ex: FeignException.Conflict) {
            logger.setException(ex)
            return showError(getErrorText(ex))
        }
    }
}
