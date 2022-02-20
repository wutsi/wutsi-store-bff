package com.wutsi.application.store.endpoint.cart.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.cart.dto.GetCartResponse
import com.wutsi.ecommerce.cart.dto.Product
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CartScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/cart?merchant-id=111"
    }

    @Test
    fun empty() {
        doReturn(GetCartResponse(Cart())).whenever(cartApi).getCart(any())

        assertEndpointEquals("/screens/cart/empty.json", url)
    }

    @Test
    fun cart() {
        val cart = Cart(
            products = listOf(
                Product(productId = 1, quantity = 1),
                Product(productId = 2, quantity = 2)
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val products = listOf(
            ProductSummary(id = 1, price = 100.0, quantity = 5, maxOrder = null),
            ProductSummary(id = 2, price = 1000.0, quantity = 10, maxOrder = 3)
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())

        assertEndpointEquals("/screens/cart/cart.json", url)
    }

    @Test
    fun outOfStock() {
        val cart = Cart(
            products = listOf(
                Product(productId = 1, quantity = 5),
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val products = listOf(
            ProductSummary(id = 1, price = 100.0, quantity = 0, maxOrder = null),
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())

        assertEndpointEquals("/screens/cart/cart-out-of-stock.json", url)
    }
}
