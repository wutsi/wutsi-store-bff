package com.wutsi.application.store.endpoint.order.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.GetShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

internal class OrderCloseScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @Test
    fun index() {
        // GIVEN
        val order = createOrder(shippingId = 111)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val shipping = createShipping(type = ShippingType.IN_STORE_PICKUP)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        // WHEN
        val url = "http://localhost:$port/order/close?id=30293209ad00"
        assertEndpointEquals("/screens/order/order-close.json", url)
    }
}
