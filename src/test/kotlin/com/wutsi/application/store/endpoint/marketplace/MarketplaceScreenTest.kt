package com.wutsi.application.store.endpoint.marketplace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.cart.dto.CartSummary
import com.wutsi.ecommerce.cart.dto.GetCartResponse
import com.wutsi.ecommerce.cart.dto.Product
import com.wutsi.ecommerce.cart.dto.SearchCartResponse
import com.wutsi.ecommerce.catalog.dto.MerchantSummary
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchMerchantResponse
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.Category
import com.wutsi.platform.account.dto.ListCategoryResponse
import com.wutsi.platform.account.dto.SearchAccountResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MarketplaceScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    private lateinit var url: String

    val merchants = listOf(
        MerchantSummary(accountId = 111),
        MerchantSummary(accountId = 222),
        MerchantSummary(accountId = 333)
    )

    val stores = listOf(
        AccountSummary(id = 111, business = true, categoryId = 111),
        AccountSummary(id = 222, business = true, categoryId = 222),
        AccountSummary(id = 333, business = true, categoryId = 333)
    )

    val categories = listOf(
        Category(id = 1, title = "Cat 1"),
        Category(id = 2, title = "Cat 2"),
        Category(id = 3, title = "Cat 3"),
        Category(id = 4, title = "Cat 4"),
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/marketplace"

        doReturn(SearchMerchantResponse(merchants)).whenever(catalogApi).searchMerchants(any())
        doReturn(SearchAccountResponse(stores)).whenever(accountApi).searchAccount(any())
        doReturn(ListCategoryResponse(categories)).whenever(accountApi).listCategories()
    }

    @Test
    fun index() {
        assertEndpointEquals("/screens/marketplace/index.json", url)
    }

    @Test
    fun empty() {
        doReturn(SearchAccountResponse()).whenever(accountApi).searchAccount(any())

        assertEndpointEquals("/screens/marketplace/empty.json", url)
    }

    @Test
    fun openedCart() {
        // WHEN
        doReturn(true).whenever(togglesProvider).isCartEnabled()

        val products = listOf(
            ProductSummary(100, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/0.png")),
            ProductSummary(101, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/1.png")),
            ProductSummary(102, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/2.png")),
            ProductSummary(103, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/3.png")),
            ProductSummary(104, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/4.png")),
            ProductSummary(105, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/5.png")),
            ProductSummary(106, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/6.png")),
            ProductSummary(107, thumbnail = PictureSummary(id = 100, url = "https://www.g.com/7.png")),
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())

        val carts = listOf(
            CartSummary(merchantId = 111)
        )
        doReturn(SearchCartResponse(carts)).whenever(cartApi).searchCarts(any())

        val cart = Cart(
            merchantId = 111,
            products = listOf(
                Product(productId = 100),
                Product(productId = 101),
                Product(productId = 103)
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        // GIVEN
        assertEndpointEquals("/screens/marketplace/opened-cart.json", url)
    }
}
