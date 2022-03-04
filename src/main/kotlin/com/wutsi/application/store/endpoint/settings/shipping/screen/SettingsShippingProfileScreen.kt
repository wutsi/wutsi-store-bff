package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping/profile")
class SettingsShippingProfileScreen(
    private val shippingApi: WutsiShippingApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
    private val cityService: CityService,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val shipping = shippingApi.getShipping(id).shipping
        val tenant = tenantProvider.get()
        val city = cityService.get(shipping.cityId)

        return Screen(
            id = Page.SETTINGS_STORE_SHIPPING_PROFILE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("shipping.type.${shipping.type}"),
            ),

            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(getText("shipping.type.${shipping.type}.description"))
                    ),
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = listOfNotNull(
                                ListItemSwitch(
                                    name = "value",
                                    caption = getText("page.settings.shipping.attribute.enabled"),
                                    selected = true,
                                    action = executeCommand(
                                        urlBuilder.build("commands/disable-shipping?id=$id")
                                    )
                                ),

                                ListItem(
                                    caption = getText("page.settings.shipping.attribute.delivery-time"),
                                    subCaption = shipping.deliveryTime?.let { formatDeliveryTime(it) },
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/delivery-time?id=$id")
                                    )
                                ),

                                ListItem(
                                    caption = getText("page.settings.shipping.attribute.rate"),
                                    subCaption = formatRate(shipping.rate, tenant),
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/rate?id=$id")
                                    )
                                ),

                                if (shipping.type == ShippingType.LOCAL_PICKUP.name || shipping.type == ShippingType.LOCAL_DELIVERY.name)
                                    ListItem(
                                        caption = getText("page.settings.shipping.attribute.city-id"),
                                        subCaption = sharedUIMapper.toLocationText(city, shipping.country ?: ""),
                                        trailing = Icon(Theme.ICON_EDIT),
                                        action = gotoUrl(
                                            urlBuilder.build("/settings/store/shipping/attribute/city-id?id=$id")
                                        )
                                    )
                                else
                                    null,

                                ListItem(
                                    caption = getText("page.settings.shipping.attribute.message"),
                                    subCaption = shipping.message ?: "",
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/message?id=$id")
                                    )
                                ),
//                                ListItem(
//                                    caption = getText("page.settings.shipping.attribute.country"),
//                                    subCaption = shipping.country?.let { Locale("en", it).getDisplayCountry(locale) },
//                                    trailing = Icon(Theme.ICON_EDIT),
//                                    action = gotoUrl(
//                                        urlBuilder.build("/settings/store/shipping/attribute/country?id=$id")
//                                    )
//                                ),
                            )
                        ),
                    )
                ),
            )
        ).toWidget()
    }
}
