package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping/attribute/rate")
class SettingsShippingProfileRateScreen(
    private val tenantProvider: TenantProvider
) : AbstractSettingsShippingProfileAttributeScreen() {
    override fun getAttributeName() = "rate"

    override fun getPageId() = Page.SETTINGS_STORE_SHIPPING_ATTRIBUTE_RATE

    override fun getInputWidget(shipping: Shipping) = Input(
        name = "value",
        value = shipping.rate?.toString() ?: "",
        type = InputType.Number,
        suffix = tenantProvider.get().currencySymbol
    )
}
