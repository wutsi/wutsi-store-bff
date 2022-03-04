package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutSuccessScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private val order = createOrder()

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())
    }

    @Test
    fun success() {
        val url = "http://localhost:$port/checkout/success?order-id=111"
        assertEndpointEquals("/screens/checkout/success.json", url)
    }

    @Test
    fun error() {
        val url = "http://localhost:$port/checkout/success?order-id=111&error=failure"
        assertEndpointEquals("/screens/checkout/error.json", url)
    }
}
