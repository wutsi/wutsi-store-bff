package com.wutsi.application.store.endpoint.order.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.entity.OrderStatus
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import import

org.springframework.boot.test.web.server.LocalServerPort

internal class OrderPickupScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @Test
    fun index() {
        // GIVEN
        val order = createOrder(shippingId = 111, status = OrderStatus.READY_FOR_PICKUP)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        // WHEN
        val url = "http://localhost:$port/order/pickup?id=555"
        assertEndpointEquals("/screens/order/order-pickup.json", url)
    }
}
