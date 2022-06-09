package com.wutsi.application.store.endpoint.order.commands

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
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
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertNull

internal class CloseOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/close-order?id=111"
    }

    @Test
    fun index() {
        // GIVEN
        val productId = 777L
        doReturn(CreateProductResponse(productId)).whenever(catalogApi).createProduct(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<ChangeStatusRequest>()
        verify(orderApi).changeStatus(eq("111"), req.capture())
        assertNull(req.firstValue.reason)
        assertNull(req.firstValue.comment)
        assertEquals(OrderStatus.DONE.name, req.firstValue.status)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)
    }
}
