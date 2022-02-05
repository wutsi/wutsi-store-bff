package com.wutsi.application.store.endpoint.settings.category.profile.screen

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsCategoryTitleScreenTest : AbstractSettingsCategoryAttributeScreenTest() {
    override fun attributeName() = "title"
}
