package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.checkout.dto.SelectShippingCountryRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@RestController
@RequestMapping("/checkout/address-editor")
class CheckoutAddressEditorScreen(
    private val cityService: CityService
) : AbstractQuery() {

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestBody request: SelectShippingCountryRequest
    ): Widget {
        val account = securityContext.currentAccount()
        val country = Locale("en", request.country).getDisplayCountry(LocaleContextHolder.getLocale())
        val cities = mutableListOf(
            DropdownMenuItem(
                caption = "",
                value = getText("page.checkout.address.editor.city")
            )
        )
        cities.addAll(
            cityService.search(null, listOf(request.country))
                .map {
                    DropdownMenuItem(
                        value = it.id.toString(),
                        caption = "${it.name}, $country"
                    )
                }
                .sortedBy { it.caption }
        )

        return Screen(
            id = Page.CHECKOUT_ADDRESS_EDITOR,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.address.editor.app-bar.title"),
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOf(
                        Container(
                            padding = 10.0,
                            alignment = Alignment.Center,
                            child = Text(
                                getText("page.checkout.address.editor.message"),
                                size = Theme.TEXT_SIZE_LARGE,
                                color = Theme.COLOR_PRIMARY
                            )
                        ),
                        Form(
                            children = listOf(
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "firstName",
                                        caption = getText("page.checkout.address.editor.first-name"),
                                        maxLength = 160,
                                        required = true
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "lastName",
                                        caption = getText("page.checkout.address.editor.last-name"),
                                        maxLength = 160,
                                        required = true
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "street",
                                        value = account.street,
                                        caption = getText("page.checkout.address.editor.street"),
                                        maxLength = 160,
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = DropdownButton(
                                        name = "cityId",
                                        value = if (account.country == request.country)
                                            account.cityId?.toString()
                                        else
                                            null,
                                        children = cities
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "submit",
                                        type = InputType.Submit,
                                        caption = getText("page.checkout.address.editor.button.submit"),
                                        action = executeCommand(
                                            urlBuilder.build("commands/save-shipping-address?order-id=$orderId&country=${request.country}")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }
}
