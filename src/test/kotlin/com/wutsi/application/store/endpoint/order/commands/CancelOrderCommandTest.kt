package com.wutsi.application.store.endpoint.order.commands

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.order.dto.CancelOrderRequest
import com.wutsi.ecommerce.catalog.dto.CreateProductResponse
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

internal class CancelOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/cancel-order?id=111"
    }

    @Test
    fun index() {
        // GIVEN
        val productId = 777L
        doReturn(CreateProductResponse(productId)).whenever(catalogApi).createProduct(any())

        // WHEN
        val request = CancelOrderRequest(
            reason = "foo",
            comment = "bar"
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<ChangeStatusRequest>()
        verify(orderApi).changeStatus(eq("111"), req.capture())
        assertEquals(request.reason, req.firstValue.reason)
        assertEquals(request.comment, req.firstValue.comment)
        assertEquals(OrderStatus.CANCELLED.name, req.firstValue.status)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)
    }
}
