package com.wutsi.application.store.endpoint.settings.product.add.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.Category
import com.wutsi.ecommerce.catalog.dto.CategorySummary
import com.wutsi.ecommerce.catalog.dto.GetCategoryResponse
import com.wutsi.ecommerce.catalog.dto.SearchCategoryRequest
import com.wutsi.ecommerce.catalog.dto.SearchCategoryResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductEditorScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/product/editor?category-id=111"

        val categories = listOf(
            CategorySummary(id = 1, title = "c1"),
            CategorySummary(id = 2, title = "c2"),
            CategorySummary(id = 3, title = "c3")
        )
        doReturn(SearchCategoryResponse(categories)).whenever(catalogApi)
            .searchCategories(SearchCategoryRequest(parentId = 111))

        val category = Category(id = 111L, title = "Foo")
        doReturn(GetCategoryResponse(category)).whenever(catalogApi).getCategory(111)
    }

    @Test
    fun index() = assertEndpointEquals("/screens/settings/product/editor.json", url)
}
