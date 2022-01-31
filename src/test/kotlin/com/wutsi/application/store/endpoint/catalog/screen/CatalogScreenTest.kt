package com.wutsi.application.store.endpoint.catalog.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.PictureSummary
import com.wutsi.platform.catalog.dto.ProductSummary
import com.wutsi.platform.catalog.dto.SearchProductResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CatalogScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/catalog"

        val products = listOf(
            createProduct(1),
            createProduct(2),
            createProduct(3),
            createProduct(4)
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProduct(any())
    }

    @Test
    fun index() = assertEndpointEquals("/screens/catalog/catalog.json", url)

    private fun createProduct(id: Long) = ProductSummary(
        id = id,
        title = "Sample product",
        summary = "Summary of product",
        price = 7000.0,
        comparablePrice = 10000.0,
        thumbnail = PictureSummary(
            id = 3,
            url = "https://www.imag.com/$id.png"
        )
    )
}
