package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.dto.CreateTransferRequest
import com.wutsi.platform.payment.dto.CreateTransferResponse
import com.wutsi.platform.payment.error.ErrorURN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import java.net.URLEncoder
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PayOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var paymentApi: WutsiPaymentApi

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

        url = "http://localhost:$port/commands/pay-order?order-id=111"
    }

    @Test
    fun index() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        doReturn(CreateTransferResponse("xxx")).whenever(paymentApi).createTransfer(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val request = argumentCaptor<CreateTransferRequest>()
        verify(paymentApi).createTransfer(request.capture())
        assertEquals(order.id, request.firstValue.orderId)
        assertEquals(order.currency, request.firstValue.currency)
        assertEquals(order.merchantId, request.firstValue.recipientId)
        assertEquals(order.totalPrice, request.firstValue.amount)
        assertNull(request.firstValue.description)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}", action.url)
    }

    @Test
    fun paymentError() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val ex = createFeignException(ErrorURN.TRANSACTION_FAILED.urn, downstreamError = ErrorCode.NOT_ENOUGH_FUNDS)
        doThrow(ex).whenever(paymentApi).createTransfer(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val message = URLEncoder.encode(getText("error.payment.NOT_ENOUGH_FUNDS"), "utf-8")
        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}&error=$message", action.url)
    }

    @Test
    fun unexpectedError() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val ex = createFeignException(ErrorURN.TRANSACTION_FAILED.urn, downstreamError = ErrorCode.UNEXPECTED_ERROR)
        doThrow(ex).whenever(paymentApi).createTransfer(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val message = URLEncoder.encode(getText("error.payment"), "utf-8")
        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}&error=$message", action.url)
    }
}
