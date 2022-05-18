package com.wutsi.application.store.endpoint.order.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.order.entity.PaymentStatus
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
internal class OrderScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private val order = Order(
        id = "111",
        merchantId = 55L,
        accountId = 1L,
        totalPrice = 25000.0,
        subTotalPrice = 30000.0,
        savingsAmount = 5000.0,
        currency = "XAF",
        status = OrderStatus.CREATED.name,
        paymentStatus = PaymentStatus.PARTIALLY_PAID.name,
        totalPaid = 20000.0,
        reservationId = 777L,
        created = OffsetDateTime.of(2020, 5, 5, 1, 1, 0, 0, ZoneOffset.UTC),
        items = listOf(
            OrderItem(productId = 1, quantity = 10, unitPrice = 100.0, unitComparablePrice = 150.0),
            OrderItem(productId = 2, quantity = 1, unitPrice = 15000.0)
        )
    )

    private val products = listOf(
        ProductSummary(id = 1L, title = "Item 1"),
        ProductSummary(id = 2L, title = "Item 2"),
    )

    private val accounts = listOf(
        AccountSummary(id = 1L, displayName = "Ray Sponsible"),
        AccountSummary(id = 55L, displayName = "John Smith")
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
        doReturn(SearchAccountResponse(accounts)).whenever(accountApi).searchAccount(any())
    }

    @Test
    fun order() {
        val url = "http://localhost:$port/order?id=111&hide-merchant=true"
        assertEndpointEquals("/screens/order/order.json", url)
    }

    @Test
    fun paymentEnabled() {
        doReturn(true).whenever(togglesProvider).isOrderPaymentEnabled()

        val url = "http://localhost:$port/order?id=111&hide-merchant=true"
        assertEndpointEquals("/screens/order/order-payment-enabled.json", url)
    }
}
