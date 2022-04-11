package com.wutsi.application.store.endpoint.settings.section.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.ListSectionResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class SettingsSectionListScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val sections = listOf(
            createSectionSummary(1, "Yo", 1),
            createSectionSummary(2, "Man", 2)
        )
        doReturn(ListSectionResponse(sections)).whenever(catalogApi).listSections(any())

        val url = "http://localhost:$port/settings/store/sections"
        assertEndpointEquals("/screens/settings/section/list.json", url)
    }
}
