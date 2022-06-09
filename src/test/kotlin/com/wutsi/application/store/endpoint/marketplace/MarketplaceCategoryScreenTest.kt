package com.wutsi.application.store.endpoint.marketplace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.Category
import com.wutsi.ecommerce.catalog.dto.GetCategoryResponse
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.platform.account.dto.SearchAccountResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import import

org.springframework.boot.test.web.server.LocalServerPort

internal class MarketplaceCategoryScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/marketplace/category?id=555"
    }

    @Test
    fun index() {
        val category = Category(id = 555L, title = "Cat 1")
        doReturn(GetCategoryResponse(category)).whenever(catalogApi).getCategory(any())

        val products = listOf(
            createProductSummary(id = 1, accountId = 11),
            createProductSummary(id = 2, accountId = 11),
            createProductSummary(id = 3, accountId = 12),
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())

        val accounts = listOf(
            createAccountSummary(id = 11, "Ray Sponsible"),
            createAccountSummary(id = 12, "Roger Milla")
        )
        doReturn(SearchAccountResponse(accounts)).whenever(accountApi).searchAccount(any())

        assertEndpointEquals("/screens/marketplace/category.json", url)
    }
}
