package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.ShippingSummary
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping")
class SettingsShippingScreen(
    private val shippingApi: WutsiShippingApi,
    private val togglesProvider: TogglesProvider,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val shippings = shippingApi.listShipping().shippings

        return Screen(
            id = Page.SETTINGS_STORE_SHIPPING,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.shipping.app-bar.title"),
            ),

            child = Column(
                children = listOf(
                    Divider(color = Theme.COLOR_DIVIDER, height = 2.0),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = listOfNotNull(
                                listItem(
                                    ShippingType.LOCAL_PICKUP,
                                    shippings
                                ),
                                listItem(
                                    ShippingType.LOCAL_DELIVERY,
                                    shippings
                                ),

                                if (togglesProvider.isShippingInternationalEnabled())
                                    listItem(
                                        ShippingType.INTERNATIONAL_SHIPPING,
                                        shippings
                                    )
                                else
                                    null,

                                if (togglesProvider.isShippingEmailEnabled())
                                    listItem(
                                        ShippingType.EMAIL_DELIVERY,
                                        shippings
                                    )
                                else
                                    null
                            )
                        )
                    )
                )
            ),
        ).toWidget()
    }

    private fun listItem(type: ShippingType, shippings: List<ShippingSummary>): WidgetAware {
        val shipping = shippings.find { it.type == type.name }
        val enabled = shipping?.enabled ?: false

        return if (enabled) {
            ListItem(
                caption = getText("shipping.type.$type"),
                subCaption = getText("shipping.type.$type.description"),
                action = gotoUrl(urlBuilder.build("settings/store/shipping/profile?id=${shipping?.id}")),
                trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT)
            )
        } else {
            val cmd = shipping?.let { "commands/enable-shipping?id=${it.id}" }
                ?: "commands/enable-shipping?type=$type"

            ListItemSwitch(
                name = "value",
                caption = getText("shipping.type.$type"),
                subCaption = getText("shipping.type.$type.description"),
                selected = false,
                action = executeCommand(urlBuilder.build(cmd))
            )
        }
    }
}
