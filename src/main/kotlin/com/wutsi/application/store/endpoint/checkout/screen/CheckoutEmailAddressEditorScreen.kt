package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/checkout/email-address-editor")
class CheckoutEmailAddressEditorScreen : AbstractQuery() {

    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
    ): Widget {
        val account = securityContext.currentAccount()

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
                                        name = "email",
                                        value = account.email,
                                        caption = getText("page.checkout.address.editor.email"),
                                        maxLength = 160,
                                        type = InputType.Email,
                                        required = true
                                    )
                                ),
                                Container(
                                    padding = 10.0,
                                    child = Input(
                                        name = "submit",
                                        type = InputType.Submit,
                                        caption = getText("page.checkout.address.editor.button.submit"),
                                        action = executeCommand(
                                            urlBuilder.build("commands/save-shipping-address?order-id=$orderId&type=EMAIL")
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
