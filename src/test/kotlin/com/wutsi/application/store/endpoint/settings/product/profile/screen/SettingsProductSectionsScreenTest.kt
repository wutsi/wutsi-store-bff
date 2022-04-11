package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetProductResponse
import com.wutsi.ecommerce.catalog.dto.ListSectionResponse
import com.wutsi.ecommerce.catalog.dto.SectionSummary
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class SettingsProductSectionsScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val product = createProduct()
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        val sections: MutableList<SectionSummary> = mutableListOf()
        sections.addAll(product.sections)
        sections.addAll(
            listOf(
                createSectionSummary(111, "Section 111", 111),
                createSectionSummary(112, "Section 112", 112),
            )
        )
        doReturn(ListSectionResponse(sections)).whenever(catalogApi).listSections(any())

        val url = "http://localhost:$port/settings/store/product/sections?id=777"
        assertEndpointEquals("/screens/settings/product/sections.json", url)
    }
}
