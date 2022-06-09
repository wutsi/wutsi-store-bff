package com.wutsi.application.store.endpoint.cart.command

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.cart.dto.UpdateCartRequest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.UpdateProductRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UpdateCartCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/update-cart?merchant-id=111&product-id=222"
    }

    @Test
    fun index() {
        // WHEN
        val request = UpdateCartRequest(quantity = 7)
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        kotlin.test.assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<UpdateProductRequest>()
        verify(cartApi).updateProduct(eq(111L), eq(222), req.capture())
        assertEquals(request.quantity, req.firstValue.quantity)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/cart?merchant-id=111", action.url)
    }
}
