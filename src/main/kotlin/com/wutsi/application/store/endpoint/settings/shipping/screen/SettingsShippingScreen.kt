package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.TenantProvider
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
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/shipping")
class SettingsShippingScreen(
    private val shippingApi: WutsiShippingApi,
    private val tenantProvider: TenantProvider,
    private val cityService: CityService
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val shippings = shippingApi.listShipping().shippings
        val tenant = tenantProvider.get()
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
                                if (togglesProvider.isShippingInStorePickup())
                                    listItem(
                                        ShippingType.IN_STORE_PICKUP,
                                        shippings,
                                        tenant
                                    )
                                else
                                    null,

                                if (togglesProvider.isShippingLocalPickupEnabled())
                                    listItem(
                                        ShippingType.LOCAL_PICKUP,
                                        shippings,
                                        tenant
                                    )
                                else
                                    null,

                                if (togglesProvider.isShippingLocalDeliveryEnabled())
                                    listItem(
                                        ShippingType.LOCAL_DELIVERY,
                                        shippings,
                                        tenant
                                    )
                                else
                                    null,

                                if (togglesProvider.isShippingInternationalDeliveryEnabled())
                                    listItem(
                                        ShippingType.INTERNATIONAL_SHIPPING,
                                        shippings,
                                        tenant
                                    )
                                else
                                    null,

                                if (togglesProvider.isShippingEmailDeliveryEnabled())
                                    listItem(
                                        ShippingType.EMAIL_DELIVERY,
                                        shippings,
                                        tenant
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

    private fun listItem(type: ShippingType, shippings: List<ShippingSummary>, tenant: Tenant): WidgetAware {
        val shipping = shippings.find { it.type == type.name }
        val enabled = shipping?.enabled ?: false

        return if (enabled) {
            ListItem(
                padding = 10.0,
                caption = getText("shipping.type.$type"),
                subCaption = toSubCaption(shipping, tenant),
                action = gotoUrl(urlBuilder.build("settings/store/shipping/profile?id=${shipping?.id}")),
                trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
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

    private fun toSubCaption(shipping: ShippingSummary?, tenant: Tenant): String? {
        if (shipping == null)
            return null

        val line1 = getText("shipping.type.${shipping.type}.description")

        val line2 = listOfNotNull(
            shipping.deliveryTime?.let { formatDeliveryTime(it) },
            formatRate(shipping.rate, tenant)
        ).joinToString(separator = " - ")

        return listOfNotNull(line1, line2).joinToString(separator = "\n")
    }
}
