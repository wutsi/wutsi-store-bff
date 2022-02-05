package com.wutsi.application.store.endpoint.settings.category.add.screen

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsCategoryAddScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/category/add"
    }

    @Test
    fun index() = assertEndpointEquals("/screens/settings/category/add.json", url)
}
