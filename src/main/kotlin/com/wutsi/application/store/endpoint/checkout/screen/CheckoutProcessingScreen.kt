package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Timeout
import com.wutsi.flutter.sdui.Widget
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/checkout/processing")
class CheckoutProcessingScreen(
    private val orderApi: WutsiOrderApi,
    private val accountApi: WutsiAccountApi,
) : AbstractQuery() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "transaction-id") transactionId: String,
    ): Widget {
        val order = orderApi.getOrder(orderId).order
        val merchant = accountApi.getAccount(order.merchantId).account

        return Screen(
            id = Page.CHECKOUT_PROCESSING,
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
            ),
            safe = true,
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
                            children = listOf(
                                Center(
                                    Container(
                                        padding = 10.0,
                                        child = Text(getText("page.checkout.processing.message"))
                                    )
                                ),
                                Timeout(
                                    url = urlBuilder.build("widgets/checkout-status?transaction-id=$transactionId"),
                                    delay = 15
                                )
                            )
                        )
                    ),
                ),
            ),
        ).toWidget()
    }
}
