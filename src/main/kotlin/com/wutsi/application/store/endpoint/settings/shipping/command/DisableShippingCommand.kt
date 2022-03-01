package com.wutsi.application.store.endpoint.settings.shipping.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.UpdateShippingAttributeRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/disable-shipping")
class DisableShippingCommand(
    val shippingApi: WutsiShippingApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
    ): Action {
        shippingApi.updateShippingAttribute(
            id,
            "enabled",
            UpdateShippingAttributeRequest(
                value = "false"
            )
        )
        return gotoUrl(urlBuilder.build("/settings/store/shipping"))
    }
}
