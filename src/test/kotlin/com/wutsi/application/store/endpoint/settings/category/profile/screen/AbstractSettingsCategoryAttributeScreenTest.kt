package com.wutsi.application.store.endpoint.settings.category.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.Category
import com.wutsi.platform.catalog.dto.GetCategoryResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal abstract class AbstractSettingsCategoryAttributeScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val category = createCategory()
        doReturn(GetCategoryResponse(category)).whenever(catalogApi).getCategory(any())
    }

    abstract fun attributeName(): String

    fun url(): String = "http://localhost:$port/settings/store/category/${attributeName()}?id=777"

    fun path(): String = "/screens/settings/category/${attributeName()}.json"

    @Test
    fun index() {
        assertEndpointEquals(path(), url())
    }

    private fun createCategory() = Category(
        title = "Sample product",
        visible = true
    )
}
