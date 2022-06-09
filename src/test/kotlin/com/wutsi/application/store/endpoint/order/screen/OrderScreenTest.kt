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
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.GetShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import import

org.springframework.boot.test.web.server.LocalServerPort
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class OrderScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    private val order = Order(
        id = "111",
        merchantId = 55L,
        accountId = 1L,
        totalPrice = 25000.0,
        subTotalPrice = 30000.0,
        savingsAmount = 5000.0,
        currency = "XAF",
        status = OrderStatus.OPENED.name,
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

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
    }

    @Test
    fun order() {
        val url = "http://localhost:$port/order?id=111"
        assertEndpointEquals("/screens/order/order.json", url)
    }

    @Test
    fun paymentEnabled() {
        doReturn(true).whenever(togglesProvider).isOrderPaymentEnabled()

        val url = "http://localhost:$port/order?id=111"
        assertEndpointEquals("/screens/order/order-payment-enabled.json", url)
    }

    @Test
    fun `in-store pickup`() {
        // GIVEN
        doReturn(true).whenever(togglesProvider).isShippingEnabled()
        doReturn(true).whenever(togglesProvider).isShippingInStorePickup()

        val shipping = createShipping(ShippingType.IN_STORE_PICKUP)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        val order = createOrder(shippingId = shipping.id, status = OrderStatus.READY_FOR_PICKUP)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        // WHEN
        val url = "http://localhost:$port/order?id=111"
        assertEndpointEquals("/screens/order/order-store-pickup.json", url)
    }
}
