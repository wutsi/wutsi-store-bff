package com.wutsi.application.store.endpoint.marketplace

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.CategorySummary
import com.wutsi.ecommerce.catalog.dto.SearchCategoryResponse
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
        val categories = listOf(
            CategorySummary(id = 1, title = "Cat 1", publishedProductCount = 10),
            CategorySummary(id = 2, title = "Cat 2", publishedProductCount = 10),
            CategorySummary(id = 3, title = "Cat 3", publishedProductCount = 0),
            CategorySummary(id = 4, title = "Cat 4", publishedProductCount = 0),
        )
        doReturn(SearchCategoryResponse(categories)).whenever(catalogApi).searchCategories(any())

        assertEndpointEquals("/screens/marketplace/marketplace.json", url)
    }
}
