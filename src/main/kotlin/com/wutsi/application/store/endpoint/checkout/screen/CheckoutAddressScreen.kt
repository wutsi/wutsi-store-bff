package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Address
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Radio
import com.wutsi.flutter.sdui.RadioGroup
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@RestController
@RequestMapping("/checkout/address")
class CheckoutAddressScreen(
    private val orderApi: WutsiOrderApi,
    private val cityService: CityService
) : AbstractQuery() {

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String
    ): Widget {
        val addresses = orderApi.listAddresses().addresses
        return Screen(
            id = Page.CHECKOUT_ADDRESS,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.address.app-bar.title"),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.center,
                crossAxisAlignment = CrossAxisAlignment.center,
                children = listOf(
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(getText("page.checkout.address.message"), size = Theme.TEXT_SIZE_LARGE)
                    ),
                    Divider(height = 1.0, color = Theme.COLOR_DIVIDER),
                    Flexible(
                        child = toAddressWidget(orderId, addresses)
                    )
                )
            )
        ).toWidget()
    }

    private fun toAddressWidget(orderId: String, addresses: List<Address>): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        if (addresses.isEmpty())
            children.add(
                Text(getText("page.checkout.address.no-address"))
            )
        else
            children.addAll(
                addresses.map {
                    Radio(
                        caption = "${it.firstName} ${it.lastName}",
                        subCaption = toAddressLabel(it),
                        value = it.id.toString()
                    )
                }
            )

        children.add(
            Container(
                padding = 10.0,
                child = Button(
                    type = ButtonType.Outlined,
                    caption = getText("page.checkout.address.button.add-address"),
                    action = gotoUrl(
                        urlBuilder.build("checkout/address-country?order-id=$orderId")
                    )
                )
            )
        )
        return RadioGroup(
            name = "addressId",
            separatorColor = Theme.COLOR_DIVIDER,
            separator = true,
            value = null,
            children = children,
            action = executeCommand(
                urlBuilder.build(
                    "commands/select-shipping-address?order-id=$orderId"
                )
            ),
        )
    }

    private fun toAddressLabel(address: Address): String {
        val city = address.cityId?.let { cityService.get(it) }
        val country = Locale("en", city?.country ?: address.country).getDisplayCountry(LocaleContextHolder.getLocale())

        return listOfNotNull(
            address.street,
            city?.let { "${it.name}, $country" } ?: country,
            address.zipCode
        )
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")
    }
}
