package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.WutsiShippingApi
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
import java.text.DecimalFormat

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
        val fmt = DecimalFormat(tenant.monetaryFormat)

        return Screen(
            id = Page.SETTINGS_STORE_SHIPPING_PROFILE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.shipping.${shipping.type}"),
            ),

            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(getText("page.settings.shipping.${shipping.type}.description"))
                    ),

                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = listOf(
                                ListItemSwitch(
                                    name = "value",
                                    caption = getText("page.settings.shipping.attribute.enabled"),
                                    selected = true,
                                    action = executeCommand(
                                        urlBuilder.build("commands/disable-shipping?id=$id")
                                    )
                                ),
                                ListItem(
                                    caption = getText("page.settings.shipping.attribute.message"),
                                    subCaption = shipping.message ?: "",
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/message?id=$id")
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
                                    subCaption = shipping.rate?.let { fmt.format(it) },
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl(
                                        urlBuilder.build("/settings/store/shipping/attribute/rate?id=$id")
                                    )
                                ),
                            )
                        ),
                    )
                ),
            )
        ).toWidget()
    }

    private fun formatDeliveryTime(value: Int): String =
        try {
            getText("shipping.delivery-time.$value")
        } catch (ex: Exception) {
            val days = value / 12
            if (days < 1)
                getText("shipping.delivery-time.less-than-1d")
            else
                getText("shipping.delivery-time.n-days")
        }
}
