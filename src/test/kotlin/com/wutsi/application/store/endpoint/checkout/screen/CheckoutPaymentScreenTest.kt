package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.platform.account.dto.ListPaymentMethodResponse
import com.wutsi.platform.account.dto.PaymentMethodSummary
import com.wutsi.platform.account.dto.Phone
import com.wutsi.platform.payment.PaymentMethodProvider
import com.wutsi.platform.payment.PaymentMethodType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutPaymentScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private val order = createOrder()

    private val paymentMethods = listOf(
        PaymentMethodSummary(
            token = "xxxx",
            type = PaymentMethodType.MOBILE.name,
            provider = PaymentMethodProvider.MTN.name,
            phone = Phone(number = "+237990000000")
        ),
        PaymentMethodSummary(
            token = "yyy",
            type = PaymentMethodType.MOBILE.name,
            provider = PaymentMethodProvider.ORANGE.name,
            phone = Phone(number = "+237990000001")
        ),
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())
        doReturn(ListPaymentMethodResponse(paymentMethods)).whenever(accountApi).listPaymentMethods(any())
    }

    @Test
    fun index() {
        val url = "http://localhost:$port/checkout/payment?order-id=111"
        assertEndpointEquals("/screens/checkout/payment.json", url)
    }
}
