package com.wutsi.application.store.endpoint.settings.shipping.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.CreateShippingRequest
import com.wutsi.ecommerce.shipping.dto.UpdateShippingAttributeRequest
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/enable-shipping")
class EnableShippingCommand(
    val shippingApi: WutsiShippingApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(required = false) id: Long? = null,
        @RequestParam(required = false) type: ShippingType? = null,
    ): Action {
        if (id == null) {
            val shippingId = shippingApi.createShipping(
                CreateShippingRequest(
                    type = type!!.name
                )
            ).id
            return gotoUrl(urlBuilder.build("/settings/store/shipping/profile?id=$shippingId"))
        } else {
            shippingApi.updateShippingAttribute(id, "enabled", UpdateShippingAttributeRequest("true"))
            return gotoUrl(urlBuilder.build("/settings/store/shipping/profile?id=$id"))
        }
    }
}
