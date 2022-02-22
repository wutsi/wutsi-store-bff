package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CancelOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/cancel-order?order-id=111"
    }

    @Test
    fun cancel() {
        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        kotlin.test.assertEquals(200, response.statusCodeValue)

        verify(orderApi).cancelOrder("111")

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)
    }

    @Test
    fun error() {
        // GIVEN
        val ex = createFeignException("xxx")
        doThrow(ex).whenever(orderApi).cancelOrder(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(orderApi).cancelOrder("111")

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(getText("error.unexpected"), action.prompt?.attributes?.get("message"))
    }
}
