package com.wutsi.application.store.endpoint.settings.category.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.CategorySummary
import com.wutsi.platform.catalog.dto.SearchCategoryResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsCategoryListScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/categories"
    }

    @Test
    fun index() {
        val category1 = CategorySummary(id = 1, title = "1")
        val category2 = CategorySummary(id = 2, title = "2")
        val category3 = CategorySummary(id = 3, title = "3")
        doReturn(SearchCategoryResponse(listOf(category1, category2, category3))).whenever(catalogApi)
            .searchCategories(any())

        assertEndpointEquals("/screens/settings/category/list.json", url)
    }
}
