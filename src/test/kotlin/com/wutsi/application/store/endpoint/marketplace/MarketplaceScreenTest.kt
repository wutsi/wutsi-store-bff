package com.wutsi.application.store.endpoint.marketplace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.MerchantSummary
import com.wutsi.ecommerce.catalog.dto.SearchMerchantResponse
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.Category
import com.wutsi.platform.account.dto.ListCategoryResponse
import com.wutsi.platform.account.dto.SearchAccountResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MarketplaceScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/marketplace"
    }

    @Test
    fun index() {
        // GIVEN
        val merchants = listOf(
            MerchantSummary(accountId = 111),
            MerchantSummary(accountId = 222),
            MerchantSummary(accountId = 333)
        )
        doReturn(SearchMerchantResponse(merchants)).whenever(catalogApi).searchMerchants(any())

        val stores = listOf(
            AccountSummary(id = 111, business = true, categoryId = 111),
            AccountSummary(id = 222, business = true, categoryId = 222),
            AccountSummary(id = 333, business = true, categoryId = 333)
        )
        doReturn(SearchAccountResponse(stores)).whenever(accountApi).searchAccount(any())

        val categories = listOf(
            Category(id = 1, title = "Cat 1"),
            Category(id = 2, title = "Cat 2"),
            Category(id = 3, title = "Cat 3"),
            Category(id = 4, title = "Cat 4"),
        )
        doReturn(ListCategoryResponse(categories)).whenever(accountApi).listCategories()

        // WHEN
        assertEndpointEquals("/screens/marketplace/marketplace.json", url)
    }
}
