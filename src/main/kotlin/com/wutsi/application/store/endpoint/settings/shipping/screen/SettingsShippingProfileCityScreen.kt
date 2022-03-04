package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.SearchableDropdown
import com.wutsi.flutter.sdui.WidgetAware
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping/attribute/city-id")
class SettingsShippingProfileCityScreen(
    private val cityService: CityService,
    private val sharedUIMapper: SharedUIMapper,
    private val tenantProvider: TenantProvider
) : AbstractSettingsShippingProfileAttributeScreen() {
    override fun getAttributeName() = "city-id"

    override fun getPageId() = Page.SETTINGS_STORE_SHIPPING_ATTRIBUTE_CITY

    override fun getInputWidget(shipping: Shipping): WidgetAware {
        val country = shipping.country ?: ""
        val children = mutableListOf<DropdownMenuItem>()
        children.add(DropdownMenuItem(caption = "", value = ""))
        children.addAll(
            cityService.search(null, listOf(country))
                .sortedBy { sharedUIMapper.toLocationText(it, country) }
                .map {
                    DropdownMenuItem(
                        caption = sharedUIMapper.toLocationText(it, country),
                        value = it.id.toString()
                    )
                }
        )
        return SearchableDropdown(
            name = "value",
            value = shipping.cityId?.toString(),
            children = children
        )
    }
}
