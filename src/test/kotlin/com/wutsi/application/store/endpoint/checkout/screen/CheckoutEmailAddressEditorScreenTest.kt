package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutEmailAddressEditorScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val url = "http://localhost:$port/checkout/email-address-editor?order-id=111"
        assertEndpointEquals("/screens/checkout/address-editor-email.json", url, emptyMap())
    }
}
