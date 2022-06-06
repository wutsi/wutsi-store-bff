package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
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
    companion object {
        const val ICON_SIZE = 80.0
    }

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
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                automaticallyImplyLeading = false,
            ),
            child = Column(
                children = listOf(
                    toSectionWidget(
                        padding = null,
                        child = ProfileListItem(
                            model = sharedUIMapper.toAccountModel(merchant),
                            showAccountType = false
                        )
                    ),
                    toSectionWidget(
                        child = Column(
                            children = listOfNotNull(
                                Container(
                                    padding = 10.0,
                                    alignment = Alignment.Center,
                                    child = getIcon(order, error)
                                ),
                                Container(
                                    alignment = Alignment.Center,
                                    child = getMessage(order, error)
                                ),
                                Container(padding = 10.0),

                                if (isPending(order, error))
                                    Button(
                                        caption = getText("page.checkout.success.button.check-status"),
                                        action = Action(
                                            type = ActionType.Route,
                                            url = urlBuilder.build("/checkout/success?order-id=$orderId")
                                        )
                                    )
                                else
                                    null,

                                Button(
                                    type = if (isPending(order, error)) ButtonType.Text else ButtonType.Elevated,
                                    caption = getText("page.checkout.success.button.submit"),
                                    action = Action(
                                        type = ActionType.Route,
                                        url = "route:/~"
                                    )
                                )
                            )
                        )
                    ),
                ),
            ),
        ).toWidget()
    }

    private fun isPending(order: Order, error: String?): Boolean =
        error == null && order.status == OrderStatus.CREATED.name

    private fun getIcon(order: Order, error: String?): Icon =
        if (error != null)
            Icon(
                code = Theme.ICON_ERROR,
                size = ICON_SIZE,
                color = Theme.COLOR_DANGER
            )
        else if (order.status == OrderStatus.CREATED.name)
            Icon(
                code = Theme.ICON_WARNING,
                size = ICON_SIZE,
                color = Theme.COLOR_WARNING
            )
        else
            Icon(
                code = Theme.ICON_CHECK_CIRCLE,
                size = ICON_SIZE,
                color = Theme.COLOR_SUCCESS
            )

    private fun getMessage(order: Order, error: String?): Text =
        if (error != null)
            Text(
                caption = getText("page.checkout.success.message-failure", arrayOf(error)),
                color = Theme.COLOR_DANGER,
                bold = true,
                alignment = TextAlignment.Center,
            )
        else if (order.status == OrderStatus.CREATED.name)
            Text(
                caption = getText("page.checkout.success.message-timeout"),
                bold = true,
                alignment = TextAlignment.Center,
            )
        else
            Text(
                caption = getText("page.checkout.success.message-success"),
                color = Theme.COLOR_SUCCESS,
                bold = true,
                alignment = TextAlignment.Center,
            )
}
