package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping/attribute/delivery-time")
class SettingsShippingProfileDeliveryTimeScreen : AbstractSettingsShippingProfileAttributeScreen() {
    override fun getAttributeName() = "delivery-time"

    override fun getPageId() = Page.SETTINGS_STORE_SHIPPING_ATTRIBUTE_DELIVERY_TIME

    override fun getInputWidget(shipping: Shipping) = DropdownButton(
        name = "value",
        value = shipping.deliveryTime?.toString(),
        children = DELIVERY_TIMES
            .map {
                DropdownMenuItem(
                    value = it,
                    caption = if (it.isEmpty()) "" else getText("shipping.delivery-time.$it"),
                )
            }
    )

    companion object {
        val DELIVERY_TIMES = listOf(
            "",
            "12",
            "24",
            "48",
            "72",
            "96",
            "120",
            "144",
            "168",
            "336",
            "504",
            "672",
        )
    }
}
