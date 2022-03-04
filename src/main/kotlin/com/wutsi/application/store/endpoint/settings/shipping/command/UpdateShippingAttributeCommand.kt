package com.wutsi.application.store.endpoint.settings.shipping.command

import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.settings.shipping.dto.AttributeRequest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.UpdateShippingAttributeRequest
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/update-shipping-attribute")
class UpdateShippingAttributeCommand(
    private val shippingApi: WutsiShippingApi,
    private val cityService: CityService
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam name: String,
        @RequestBody request: AttributeRequest
    ): Action {
        shippingApi.updateShippingAttribute(
            id,
            name,
            UpdateShippingAttributeRequest(
                value = request.value
            )
        )

        if (name == "city-id") {
            val city = cityService.get(request.value.toLong())
            if (city != null)
                shippingApi.updateShippingAttribute(
                    id,
                    "country",
                    UpdateShippingAttributeRequest(
                        value = city.country
                    )
                )
        }

        return gotoPreviousScreen()
    }
}
