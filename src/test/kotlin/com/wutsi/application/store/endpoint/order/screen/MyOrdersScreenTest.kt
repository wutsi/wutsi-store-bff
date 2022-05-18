package com.wutsi.application.store.endpoint.order.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.dto.SearchOrderResponse
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MyOrdersScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private lateinit var url: String

    private val orders = listOf(
        OrderSummary(
            id = "111",
            merchantId = 55L,
            accountId = 111L,
            totalPrice = 25000.0,
            subTotalPrice = 30000.0,
            savingsAmount = 5000.0,
            currency = "XAF",
            status = OrderStatus.COMPLETED.name,
            reservationId = 777L,
            created = OffsetDateTime.of(2020, 5, 5, 1, 1, 0, 0, ZoneOffset.UTC),
        ),
        OrderSummary(
            id = "222",
            merchantId = 66L,
            accountId = 222L,
            totalPrice = 50000.0,
            subTotalPrice = 30000.0,
            savingsAmount = 5000.0,
            currency = "XAF",
            status = OrderStatus.PROCESSING.name,
            reservationId = 777L,
            created = OffsetDateTime.of(2020, 6, 5, 1, 1, 0, 0, ZoneOffset.UTC),
        ),
    )

    private val merchants = listOf(
        AccountSummary(id = 55L, displayName = "Ray Sponsible", business = true),
        AccountSummary(id = 66L, displayName = "John Smith", business = true)
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/me/orders"
    }

    @Test
    fun index() {
        doReturn(SearchOrderResponse(orders)).whenever(orderApi).searchOrders(any())
        doReturn(SearchAccountResponse(merchants)).whenever(accountApi).searchAccount(any())

        assertEndpointEquals("/screens/order/my-orders.json", url)
    }
}
