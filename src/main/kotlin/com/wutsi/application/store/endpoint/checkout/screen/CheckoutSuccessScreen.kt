package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.payment.WutsiPaymentApi
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
    private val paymentApi: WutsiPaymentApi,
) : AbstractQuery() {
    @PostMapping
    fun index(
        @RequestParam(name = "order-id") orderId: String,
        @RequestParam(name = "transaction-id", required = false) transactionId: String? = null,
        @RequestParam(required = false) error: String? = null,
        request: HttpServletRequest,
    ): Widget {
        val order = orderApi.getOrder(orderId).order
        val merchant = accountApi.getAccount(order.merchantId).account

        return Screen(
            id = error?.let { Page.CHECKOUT_ERROR } ?: Page.CHECKOUT_SUCCESS,
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            appBar = null,
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
                        child = if (error != null)
                            toTransactionStatusWidget(null, error)
                        else
                            toTransactionStatusWidget(
                                tx = paymentApi.getTransaction(transactionId!!).transaction
                            )
                    ),
                ),
            ),
        ).toWidget()
    }
}
