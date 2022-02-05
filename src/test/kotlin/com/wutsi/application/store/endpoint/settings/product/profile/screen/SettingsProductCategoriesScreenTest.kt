package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.catalog.dto.CategorySummary
import com.wutsi.platform.catalog.dto.SearchCategoryResponse
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductCategoriesScreenTest : AbstractSettingsProductAttributeScreenTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        val categories = listOf(
            CategorySummary(1, "c1"),
            CategorySummary(2, "c2"),
            CategorySummary(3, "c3")
        )
        doReturn(SearchCategoryResponse(categories)).whenever(catalogApi).searchCategories(any())
    }

    override fun attributeName() = "categories"
}
