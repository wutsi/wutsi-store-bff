package com.wutsi.application.store.endpoint.settings.shipping.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.CreateShippingRequest
import com.wutsi.ecommerce.shipping.dto.CreateShippingResponse
import com.wutsi.ecommerce.shipping.dto.UpdateShippingAttributeRequest
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class EnableShippingCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @Test
    fun create() {
        doReturn(CreateShippingResponse(111)).whenever(shippingApi).createShipping(any())

        val url = "http://localhost:$port/commands/enable-shipping?type=${ShippingType.INTERNATIONAL_SHIPPING}"
        val response = rest.postForEntity(url, null, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals("http://localhost:0/settings/store/shipping/profile?id=111", action.url)
        assertEquals(ActionType.Route, action.type)

        verify(shippingApi).createShipping(
            CreateShippingRequest(
                type = ShippingType.INTERNATIONAL_SHIPPING.name
            )
        )
    }

    @Test
    fun enable() {
        doReturn(CreateShippingResponse(111)).whenever(shippingApi).createShipping(any())

        val url = "http://localhost:$port/commands/enable-shipping?id=111"
        val response = rest.postForEntity(url, null, Action::class.java)

        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals("http://localhost:0/settings/store/shipping/profile?id=111", action.url)
        assertEquals(ActionType.Route, action.type)

        verify(shippingApi).updateShippingAttribute(111L, "enabled", UpdateShippingAttributeRequest("true"))
    }
}
