package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@RestController
@RequestMapping("/checkout/address-country")
class CheckoutAddressCountryScreen : AbstractQuery() {

    @PostMapping
    fun index(@RequestParam(name = "order-id") orderId: String): Widget {
        val account = securityContext.currentAccount()
        val locale = LocaleContextHolder.getLocale()

        return Screen(
            id = Page.CHECKOUT_ADDRESS_COUNTRY,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.address.country.app-bar.title"),
            ),
            child = SingleChildScrollView(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.center,
                    crossAxisAlignment = CrossAxisAlignment.center,
                    children = listOf(
                        Form(
                            children = listOf(
                                Container(
                                    padding = 10.0,
                                    alignment = Alignment.Center,
                                    child = Text(
                                        getText("page.checkout.address.country.message"),
                                        size = Theme.TEXT_SIZE_LARGE,
                                        color = Theme.COLOR_PRIMARY
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = DropdownButton(
                                        name = "country",
                                        value = account.country,
                                        required = true,
                                        children = Locale.getISOCountries()
                                            .map {
                                                DropdownMenuItem(
                                                    value = it,
                                                    caption = Locale("en", it).getDisplayCountry(locale)
                                                )
                                            }
                                            .sortedBy { it.caption }
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "submit",
                                        type = InputType.Submit,
                                        caption = getText("page.checkout.address.country.button.submit"),
                                        action = gotoUrl(
                                            urlBuilder.build("/checkout/address-editor?order-id=$orderId")
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
