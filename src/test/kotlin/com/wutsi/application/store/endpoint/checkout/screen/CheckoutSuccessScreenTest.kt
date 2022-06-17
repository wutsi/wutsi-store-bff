package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.TrackingHttpRequestInterceptor
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.GetTransactionResponse
import com.wutsi.platform.payment.dto.Transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutSuccessScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var paymentApi: WutsiPaymentApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        rest.interceptors.add(
            TrackingHttpRequestInterceptor(
                userAgent = "Android",
                referer = "https://www.google.com",
                ip = "10.0.2.2"
            )
        )
    }

    @Test
    fun success() {
        val order = createOrder(status = OrderStatus.OPENED)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val tx = createTransaction(status = Status.SUCCESSFUL)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        val url = "http://localhost:$port/checkout/success?order-id=${order.id}&transaction-id=${tx.id}"
        assertEndpointEquals("/screens/checkout/success.json", url)
    }

    @Test
    fun error() {
        val order = createOrder(status = OrderStatus.CREATED)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val url = "http://localhost:$port/checkout/success?order-id=${order.id}&error=failure"
        assertEndpointEquals("/screens/checkout/error.json", url)
    }

    @Test
    fun timeout() {
        val order = createOrder(status = OrderStatus.CREATED)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val tx = createTransaction(status = Status.PENDING)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        val url = "http://localhost:$port/checkout/success?order-id=${order.id}&transaction-id=${tx.id}"
        assertEndpointEquals("/screens/checkout/timeout.json", url)
    }

    private fun createTransaction(status: Status) = Transaction(
        status = status.name
    )
}
