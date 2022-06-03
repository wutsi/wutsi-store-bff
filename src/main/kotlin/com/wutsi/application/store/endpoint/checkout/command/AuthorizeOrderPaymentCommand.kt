package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.checkout.dto.AuthorizeOrderPaymentRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/authorize-order-payment")
class AuthorizeOrderPaymentCommand(
    private val accountApi: WutsiAccountApi,
    @Value("\${wutsi.application.login-url}") private val loginUrl: String
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestBody request: AuthorizeOrderPaymentRequest,
    ): Action {
        val me = accountApi.getAccount(securityContext.currentAccountId()).account
        val path = "?phone=" + encodeURLParam(me.phone!!.number) +
            "&icon=" + Theme.ICON_LOCK +
            "&screen-id=" + Page.CHECKOUT_PIN +
            "&title=" + encodeURLParam(getText("page.checkout-pin.title")) +
            "&sub-title=" + encodeURLParam(getText("page.checkout-pin.sub-title")) +
            "&auth=false" +
            "&return-to-route=false" +
            "&return-url=" + encodeURLParam(
            urlBuilder.build(
                "commands/pay-order?order-id=$orderId&payment-token=${request.paymentToken}"
            )
        )
        return gotoUrl(urlBuilder.build(loginUrl, path))
    }
}
