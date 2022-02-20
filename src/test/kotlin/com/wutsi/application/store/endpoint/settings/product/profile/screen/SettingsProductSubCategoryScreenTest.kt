package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductSubCategoryScreenTest : AbstractSettingsProductAttributeScreenTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        val product1 = ProductSummary(id = 1, title = "1", summary = "Short description of product1")
        val product2 = ProductSummary(id = 2, title = "2")
        val product3 = ProductSummary(id = 3, title = "3", thumbnail = PictureSummary(url = "http://u.com/1.png"))
        doReturn(SearchProductResponse(listOf(product1, product2, product3))).whenever(catalogApi).searchProducts(any())
    }

    override fun attributeName() = "sub-category-id"
}
