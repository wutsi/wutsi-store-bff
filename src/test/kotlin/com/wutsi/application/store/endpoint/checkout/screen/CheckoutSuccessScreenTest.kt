package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.ecommerce.order.entity.OrderStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutSuccessScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private val order = Order(
        id = "111",
        merchantId = 55L,
        totalPrice = 25000.0,
        subTotalPrice = 30000.0,
        savingsAmount = 5000.0,
        currency = "XAF",
        status = OrderStatus.CREATED.name,
        reservationId = 777L,
        items = listOf(
            OrderItem(productId = 1, quantity = 10, unitPrice = 100.0, unitComparablePrice = 150.0),
            OrderItem(productId = 2, quantity = 1, unitPrice = 15000.0)
        )
    )

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
