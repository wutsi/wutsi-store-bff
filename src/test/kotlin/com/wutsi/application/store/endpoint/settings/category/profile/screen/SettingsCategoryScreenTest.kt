package com.wutsi.application.store.endpoint.settings.category.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.Category
import com.wutsi.platform.catalog.dto.GetCategoryResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsCategoryScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/category?id=11"

        val category = Category(
            id = 11L,
            visible = true,
            title = "Hello worls"
        )
        doReturn(GetCategoryResponse(category)).whenever(catalogApi).getCategory(any())
    }

    @Test
    fun index() = assertEndpointEquals("/screens/settings/category/profile.json", url)
}
