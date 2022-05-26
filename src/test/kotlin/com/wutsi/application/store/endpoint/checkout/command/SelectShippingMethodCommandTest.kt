package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.service.ShippingService
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.SetShippingMethodRequest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.RateSummary
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SelectShippingMethodCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var service: ShippingService

    @Test
    fun select() {
        // GIVEN
        val order = createOrder()
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val rate = RateSummary(
            shippingId = 555,
            shippingType = ShippingType.LOCAL_PICKUP.name,
            rate = 1500.0,
            currency = "XAF"
        )
        doReturn(rate).whenever(service).findShippingRate(any(), any(), any())

        // WHEN
        val url = "http://localhost:$port/commands/select-shipping-method?order-id=111&shipping-id=555"
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<SetShippingMethodRequest>()
        verify(orderApi).setShippingMethod(eq("111"), req.capture())
        assertEquals(rate.shippingId, req.firstValue.shippingId)
        assertEquals(rate.deliveryTime, req.firstValue.deliveryTime)
        assertEquals(rate.rate, req.firstValue.deliveryFees)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/review?order-id=111", action.url)
    }
}
