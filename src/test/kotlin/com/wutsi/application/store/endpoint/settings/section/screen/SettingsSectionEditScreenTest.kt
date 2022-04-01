package com.wutsi.application.store.endpoint.settings.section.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetSectionResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class SettingsSectionEditScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        // GIVEN
        val section = createSection()
        doReturn(GetSectionResponse(section)).whenever(catalogApi).getSection(any())

        // WHEN
        val url = "http://localhost:$port/settings/store/section/edit?id=${section.id}"
        assertEndpointEquals("/screens/settings/section/edit.json", url)
    }
}
