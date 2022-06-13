package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order/start-delivery")
class OrderStartDeliveryScreen : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam(name = "id") id: String): Widget {
        val xid = id.uppercase().takeLast(4)
        return Screen(
            id = Page.ORDER_START_DELIVERY,
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_PRIMARY,
                foregroundColor = Theme.COLOR_WHITE,
                title = getText("page.order.start-delivery.app-bar.title", arrayOf(xid)),
            ),
            bottomNavigationBar = bottomNavigationBar(),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOfNotNull(
                    Container(padding = 30.0),
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.order.start-delivery.message"),
                            size = Theme.TEXT_SIZE_LARGE,
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.order.start-delivery.message-notification"),
                            color = Theme.COLOR_PRIMARY
                        )
                    ),

                    Container(padding = 10.0),
                    Container(
                        padding = 10.0,
                        child = Button(
                            caption = getText("page.order.start-delivery.button.start"),
                            action = executeCommand(urlBuilder.build("commands/start-order-delivery?id=$id"))
                        )
                    ),
                    Button(
                        type = ButtonType.Text,
                        caption = getText("page.order.start-delivery.button.not-now"),
                        action = gotoPreviousScreen()
                    )
                )
            ),
        ).toWidget()
    }
}
