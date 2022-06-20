package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.CreateOrderItem
import com.wutsi.ecommerce.order.dto.CreateOrderRequest
import com.wutsi.ecommerce.order.entity.AddressType
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.logging.KVLogger
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/buy-now")
class BuyOrderCommand(
    private val orderApi: WutsiOrderApi,
    private val logger: KVLogger,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "merchant-id") merchantId: Long,
        @RequestParam(name = "product-id") productId: Long,
        @RequestParam(name = "address-type") addressType: AddressType,
    ): Action {
        try {
            val orderId = orderApi.createOrder(
                CreateOrderRequest(
                    addressType = addressType.name,
                    merchantId = merchantId,
                    items = listOf(
                        CreateOrderItem(
                            productId = productId,
                            quantity = 1
                        )
                    ),
                )
            ).id
            logger.add("order_id", orderId)

            val shippingEnabled = togglesProvider.isShippingEnabled()
            logger.add("shipping_enabled", shippingEnabled)
            return gotoUrl(
                url = if (shippingEnabled)
                    urlBuilder.build("checkout/address?order-id=$orderId")
                else
                    urlBuilder.build("checkout/review?order-id=$orderId")
            )
        } catch (ex: FeignException.Conflict) {
            logger.setException(ex)
            return showError(getErrorText(ex))
        }
    }
}
