package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class OrderCloseScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val url = "http://localhost:$port/order/close?id=30293209ad00"
        assertEndpointEquals("/screens/order/order-close.json", url)
    }
}
