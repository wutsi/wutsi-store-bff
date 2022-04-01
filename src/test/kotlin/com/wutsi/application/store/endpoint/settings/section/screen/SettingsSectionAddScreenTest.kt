package com.wutsi.application.store.endpoint.settings.section.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class SettingsSectionAddScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val url = "http://localhost:$port/settings/store/section/add"
        assertEndpointEquals("/screens/settings/section/add.json", url)
    }
}
