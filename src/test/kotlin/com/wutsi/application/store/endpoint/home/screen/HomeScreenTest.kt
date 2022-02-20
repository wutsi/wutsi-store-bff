package com.wutsi.application.store.endpoint.home.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetProductResponse
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.cart.WutsiCartApi
import com.wutsi.platform.cart.dto.Cart
import com.wutsi.platform.cart.dto.GetCartResponse
import com.wutsi.platform.cart.dto.Product
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class HomeScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @MockBean
    private lateinit var togglesProvider: TogglesProvider

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val products = listOf(
            createProductSummary(1),
            createProductSummary(2),
            createProductSummary(3),
            createProductSummary(4)
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
    }

    @Test
    fun myCatalog() {
        val url = "http://localhost:$port"
        assertEndpointEquals("/screens/home/catalog-me.json", url)
    }

    @Test
    fun otherCatalog() {
        val accountId = 9L
        val url = "http://localhost:$port?id=$accountId"
        val account = createAccount(accountId)
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(accountId)
        assertEndpointEquals("/screens/home/catalog-other.json", url)
    }

    @Test
    fun cartEnabled() {
        doReturn(true).whenever(togglesProvider).isCartEnabled()

        val cart = Cart(
            products = listOf(
                Product(productId = 1L, quantity = 1),
                Product(productId = 2L, quantity = 3)
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        val url = "http://localhost:$port"
        assertEndpointEquals("/screens/home/catalog-with-cart-enabled.json", url)
    }
}
