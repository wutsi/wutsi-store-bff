package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.ProfileCard
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/checkout/success")
class CheckoutSuccessScreen(
    private val orderApi: WutsiOrderApi,
    private val accountApi: WutsiAccountApi,
) : AbstractQuery() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(required = false) error: String? = null,
        request: HttpServletRequest,
    ): Widget {
        val order = orderApi.getOrder(orderId).order
        val merchant = accountApi.getAccount(order.merchantId).account

        return Screen(
            id = error?.let { Page.CHECKOUT_ERROR } ?: Page.CHECKOUT_SUCCESS,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                automaticallyImplyLeading = false,
            ),
            child = Column(
                children = listOf(
                    Center(
                        child = ProfileCard(
                            model = sharedUIMapper.toAccountModel(merchant),
                            showWebsite = false,
                            showPhoneNumber = false
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    Container(
                        alignment = Alignment.Center,
                        child = Icon(
                            code = error?.let { Theme.ICON_ERROR } ?: Theme.ICON_CHECK_CIRCLE,
                            size = 80.0,
                            color = error?.let { Theme.COLOR_DANGER } ?: Theme.COLOR_SUCCESS
                        )
                    ),
                    Container(
                        alignment = Alignment.Center,
                        padding = 10.0,
                        child = Text(
                            error?.let { getText("page.checkout.success.failed", arrayOf(it)) }
                                ?: getText("page.checkout.success.message"),
                            color = error?.let { Theme.COLOR_DANGER } ?: Theme.COLOR_SUCCESS,
                            alignment = TextAlignment.Center,
                            bold = true
                        ),
                    ),
                    Container(
                        padding = 10.0,
                        child = Button(
                            caption = getText("page.checkout.success.button.submit"),
                            action = Action(
                                type = ActionType.Route,
                                url = "route:/~"
                            )
                        )
                    )
                ),
            )
        ).toWidget()
    }
}
