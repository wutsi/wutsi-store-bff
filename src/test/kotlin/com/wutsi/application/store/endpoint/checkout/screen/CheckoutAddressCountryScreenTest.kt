package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutAddressCountryScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val url = "http://localhost:$port/checkout/address-country?order-id=111"
        assertEndpointEquals("/screens/checkout/address-country.json", url)
    }
}
