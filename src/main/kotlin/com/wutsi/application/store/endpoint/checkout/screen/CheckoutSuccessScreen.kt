package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.flutter.sdui.Widget
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/checkout/success")
class CheckoutSuccessScreen() : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam(name = "order-id") merchantId: Long): Widget = TODO()
}
