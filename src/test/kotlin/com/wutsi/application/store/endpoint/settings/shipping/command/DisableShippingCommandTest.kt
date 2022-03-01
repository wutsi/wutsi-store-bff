package com.wutsi.application.store.endpoint.settings.shipping.command

import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.UpdateShippingAttributeRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DisableShippingCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @Test
    fun index() {
        val url = "http://localhost:$port/commands/disable-shipping?id=11"
        val response = rest.postForEntity(url, null, Action::class.java)

        val action = response.body!!
        assertEquals("http://localhost:0/settings/store/shipping", action.url)
        assertEquals(ActionType.Route, action.type)

        verify(shippingApi).updateShippingAttribute(11L, "enabled", UpdateShippingAttributeRequest("false"))
    }
}
