package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.Shipping
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
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val shipping = shippingApi.getShipping(id).shipping
        val tenant = tenantProvider.get()

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
                                    caption = toCaption("delivery-time", shipping),
                                    subCaption = shipping.deliveryTime?.let { formatDeliveryTime(it) },
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/delivery-time?id=$id")
                                    )
                                ),

                                if (isPriceRequired(shipping))
                                    ListItem(
                                        caption = toCaption("rate", shipping),
                                        subCaption = formatRate(shipping.rate, tenant),
                                        trailing = Icon(Theme.ICON_EDIT),
                                        action = gotoUrl(
                                            urlBuilder.build("/settings/store/shipping/attribute/rate?id=$id")
                                        )
                                    )
                                else
                                    null,

                                if (isCityRequired(shipping))
                                    ListItem(
                                        caption = toCaption("city-id", shipping),
                                        subCaption = formatRate(shipping.rate, tenant),
                                        trailing = Icon(Theme.ICON_EDIT),
                                        action = gotoUrl(
                                            urlBuilder.build("/settings/store/shipping/attribute/city-id?id=$id")
                                        )
                                    )
                                else
                                    null,

                                ListItem(
                                    caption = toCaption("message", shipping),
                                    subCaption = shipping.message ?: "",
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/message?id=$id")
                                    )
                                ),
                            )
                        ),
                    )
                ),
            )
        ).toWidget()
    }

    private fun isPriceRequired(shipping: Shipping): Boolean =
        shipping.type == ShippingType.LOCAL_DELIVERY.name ||
            shipping.type == ShippingType.INTERNATIONAL_SHIPPING.name ||
            shipping.type == ShippingType.LOCAL_PICKUP.name

    private fun isCityRequired(shipping: Shipping): Boolean =
        shipping.type == ShippingType.LOCAL_DELIVERY.name

    private fun toCaption(name: String, shipping: Shipping): String =
        try {
            getText("page.settings.shipping.attribute.$name.${shipping.type}")
        } catch (ex: Exception) {
            getText("page.settings.shipping.attribute.$name")
        }
}
