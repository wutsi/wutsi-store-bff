package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.service.ShippingService
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.shipping.dto.RateSummary
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutShippingScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var service: ShippingService

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val order = createOrder()
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val rates = listOf(
            RateSummary(
                shippingId = 1L,
                shippingType = ShippingType.LOCAL_PICKUP.name,
                rate = 1500.0,
                currency = "XAF"
            ),
            RateSummary(
                shippingId = 2L,
                shippingType = ShippingType.INTERNATIONAL_SHIPPING.name,
                rate = 15000.0,
                currency = "XAF"
            )
        )
        doReturn(rates).whenever(service).findShippingRates(any(), any())
    }

    @Test
    fun success() {
        val url = "http://localhost:$port/checkout/shipping?order-id=111"
        assertEndpointEquals("/screens/checkout/shipping.json", url)
    }
}
