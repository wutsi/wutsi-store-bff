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
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
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
        val children = mutableListOf<WidgetAware>()
        children.addAll(
            listOf(
                Container(
                    padding = 10.0,
                    alignment = Alignment.Center,
                    child = Text(
                        getText("page.checkout.address.message"),
                        size = Theme.TEXT_SIZE_LARGE,
                    )
                ),
                Divider(height = 1.0, color = Theme.COLOR_DIVIDER)
            )
        )

        val addresses = orderApi.listAddresses().addresses
        children.addAll(toAddressWidget(orderId, addresses))

        return Screen(
            id = Page.CHECKOUT_ADDRESS,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.address.app-bar.title"),
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children
                )
            )
        ).toWidget()
    }

    private fun toAddressWidget(orderId: String, addresses: List<Address>): List<WidgetAware> {
        val children = mutableListOf<WidgetAware>()
        if (addresses.isEmpty())
            children.addAll(
                listOf(
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(
                            getText("page.checkout.address.no-address"),
                            alignment = TextAlignment.Center
                        )
                    ),
                    Button(
                        caption = getText("page.checkout.address.button.add-address"),
                        action = gotoUrl(
                            urlBuilder.build("checkout/address-country?order-id=$orderId")
                        )
                    )
                )
            )
        else {
            children.addAll(
                addresses.map {
                    Container(
                        alignment = Alignment.TopLeft,
                        borderRadius = 4.0,
                        border = 1.0,
                        borderColor = Theme.COLOR_DIVIDER,
                        padding = 10.0,
                        margin = 10.0,
                        width = Double.MAX_VALUE, /* Full width */
                        child = Column(
                            mainAxisAlignment = MainAxisAlignment.start,
                            crossAxisAlignment = CrossAxisAlignment.start,
                            children = listOf(
                                Text("${it.firstName} ${it.lastName}", bold = true),
                                Text(toAddressLabel(it)),
                            )
                        ),
                        action = executeCommand(
                            urlBuilder.build("commands/select-shipping-address?order-id=$orderId&address-id=${it.id}")
                        ),
                    )
                }
            )

            children.add(Divider(color = Theme.COLOR_DIVIDER))
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
        }

        return children
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
