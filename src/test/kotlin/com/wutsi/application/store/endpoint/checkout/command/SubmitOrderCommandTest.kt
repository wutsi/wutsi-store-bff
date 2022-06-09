package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.payment.WutsiPaymentApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URLEncoder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SubmitOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var paymentApi: WutsiPaymentApi

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    private lateinit var url: String

    private val order = Order(
        id = "111",
        merchantId = 55L,
        totalPrice = 1000.0,
        currency = "XAF",
        status = OrderStatus.CREATED.name,
        reservationId = 777L,
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/submit-order?order-id=111"
    }

    @Test
    fun index() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(cartApi).emptyCart(order.merchantId)
        verify(orderApi).changeStatus("111", ChangeStatusRequest(status = OrderStatus.OPENED.name))

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}", action.url)
    }

    @Test
    fun unexpectedError() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val ex = createFeignException(com.wutsi.ecommerce.order.error.ErrorURN.ILLEGAL_STATUS.urn)
        doThrow(ex).whenever(orderApi).changeStatus(any(), any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val message = URLEncoder.encode(getText("error.unexpected"), "utf-8")
        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}&error=$message", action.url)

        verify(cartApi, never()).emptyCart(any())
    }
}
