package com.wutsi.application.store.endpoint.settings.product.list.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.CategorySummary
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchCategoryResponse
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductListScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/products"

        val category1 = CategorySummary(id = 1, title = "Category1")
        val category2 = CategorySummary(id = 2, title = "Category2")
        doReturn(SearchCategoryResponse(listOf(category1, category2))).whenever(catalogApi).searchCategories(any())
    }

    @Test
    fun index() {
        val product1 = ProductSummary(id = 1, title = "1", summary = "Short description of product1")
        val product2 = ProductSummary(id = 2, title = "2")
        val product3 = ProductSummary(id = 3, title = "3", thumbnail = PictureSummary(url = "http://u.com/1.png"))
        doReturn(SearchProductResponse(listOf(product1, product2, product3))).whenever(catalogApi).searchProducts(any())

        assertEndpointEquals("/screens/settings/product/list.json", url)
    }
}
