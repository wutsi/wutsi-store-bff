package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.Theme
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

@RestController
@RequestMapping("/settings/store/shipping/profile")
class SettingsShippingProfileScreen(
    private val shippingApi: WutsiShippingApi
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val shipping = shippingApi.getShipping(id).shipping

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
                                    caption = getText("page.settings.shipping.enabled"),
                                    selected = true,
                                    action = executeCommand(
                                        urlBuilder.build("commands/disable-shipping?id=$id")
                                    )
                                ),
                                ListItem(
                                    caption = getText("page.settings.shipping.message"),
                                    subCaption = shipping.message ?: "",
                                    trailing = Icon(Theme.ICON_EDIT),
                                    action = gotoUrl("/settings/store/shipping/attribute/message?id$id")
                                ),
                            )
                        )
                    )
                )
            ),
        ).toWidget()
    }
}
