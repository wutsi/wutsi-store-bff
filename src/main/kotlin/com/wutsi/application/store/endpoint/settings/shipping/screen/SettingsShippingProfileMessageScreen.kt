package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.flutter.sdui.Input
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping/attribute/message")
class SettingsShippingProfileMessageScreen : AbstractSettingsShippingProfileAttributeScreen() {
    override fun getAttributeName() = "message"

    override fun getPageId() = Page.SETTINGS_STORE_SHIPPING_ATTRIBUTE_MESSAGE

    override fun getInputWidget(shipping: Shipping) = Input(
        name = "value",
        value = shipping.message,
        maxLines = 5
    )
}
