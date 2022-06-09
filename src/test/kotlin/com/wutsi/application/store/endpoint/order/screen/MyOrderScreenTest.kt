package com.wutsi.application.store.endpoint.order.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.entity.CityEntity
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.GetShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MyOrderScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var cityService: CityService

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    private val products = listOf(
        ProductSummary(id = 1L, title = "Item 1"),
        ProductSummary(id = 2L, title = "Item 2"),
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
    }

    @Test
    fun `no shipping`() {
        // GIVEN
        val order = createOrder(shippingId = null)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        // WHEN
        val url = "http://localhost:$port/me/order?id=111"
        assertEndpointEquals("/screens/order/my-order-no-shipping.json", url)
    }

    @Test
    fun `in-store pickup`() {
        // GIVEN
        doReturn(true).whenever(togglesProvider).isShippingEnabled()
        doReturn(true).whenever(togglesProvider).isShippingInStorePickup()

        val city = CityEntity(id = 111, name = "Yaounde", country = "CM")
        doReturn(city).whenever(cityService).get(any())

        val shipping = createShipping(ShippingType.IN_STORE_PICKUP)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        val order = createOrder(shippingId = shipping.id, status = OrderStatus.READY_FOR_PICKUP)
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        // WHEN
        val url = "http://localhost:$port/me/order?id=111"
        assertEndpointEquals("/screens/order/my-order-store-pickup.json", url)
    }
}
