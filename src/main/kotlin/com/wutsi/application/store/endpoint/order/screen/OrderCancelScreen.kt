package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order/cancel")
class OrderCancelScreen : AbstractQuery() {
    companion object {
        private val REASONS = listOf(
            "change_of_mind",
            "high_shipping_costs",
            "long_delivery_times",
            "high_service_charges",
            "payment_failure",
            "other"
        )
    }

    @PostMapping
    fun index(@RequestParam(name = "id") id: String): Widget {
        val xid = id.uppercase().takeLast(4)
        return Screen(
            id = Page.ORDER_CANCEL,
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_PRIMARY,
                foregroundColor = Theme.COLOR_WHITE,
                title = getText("page.order.cancel.app-bar.title", arrayOf(xid)),
            ),
            bottomNavigationBar = bottomNavigationBar(),
            child = Form(
                children = listOf(
                    Container(padding = 10.0),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(
                            caption = getText("page.order.cancel.message"),
                            size = Theme.TEXT_SIZE_LARGE,
                        )
                    ),

                    Container(padding = 10.0),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.CenterLeft,
                        width = Double.MAX_VALUE,
                        child = Text(getText("page.order.cancel.reason"))
                    ),
                    Container(
                        padding = 10.0,
                        child = DropdownButton(
                            name = "reason",
                            value = null,
                            required = true,
                            children = REASONS.map {
                                DropdownMenuItem(
                                    value = it,
                                    caption = getText("page.order.cancel.reason.$it")
                                )
                            }
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "comment",
                            caption = getText("page.order.cancel.comment"),
                        )
                    ),

                    Container(padding = 10.0),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "submit",
                            type = InputType.Submit,
                            caption = getText("page.order.cancel.button.close"),
                            action = executeCommand(urlBuilder.build("commands/cancel-order?id=$id"))
                        )
                    ),
                    Button(
                        type = ButtonType.Text,
                        caption = getText("page.order.cancel.button.not-now"),
                        action = gotoPreviousScreen()
                    )
                )
            ),
        ).toWidget()
    }
}
