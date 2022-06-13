package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort

internal class OrderStartDeliveryScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/order/start-delivery?id=111"
    }

    @Test
    fun index() {
        assertEndpointEquals("/screens/order/status/start-delivery.json", url)
    }
}
