package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.Test
import import

org.springframework.boot.test.web.server.LocalServerPort

internal class OrderCancelScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val url = "http://localhost:$port/order/cancel?id=30293209ad00"
        assertEndpointEquals("/screens/order/order-cancel.json", url)
    }
}
