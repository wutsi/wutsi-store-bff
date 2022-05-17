package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.GetShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutReviewScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    private val order = createOrder()

    private val products = listOf(
        ProductSummary(id = 1, title = "Item 1"),
        ProductSummary(id = 2, title = "Item 2"),
    )

    private val shipping = createShipping(ShippingType.LOCAL_PICKUP)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())
    }

    @Test
    fun index() {
        val url = "http://localhost:$port/checkout/review?order-id=111"
        assertEndpointEquals("/screens/checkout/review.json", url)
    }

    @Test
    fun paymentEnabled() {
        doReturn(true).whenever(togglesProvider).isPaymentEnabled()

        val url = "http://localhost:$port/checkout/review?order-id=111"
        assertEndpointEquals("/screens/checkout/review-payment-enabled.json", url)
    }
}
