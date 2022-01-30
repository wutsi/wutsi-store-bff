package com.wutsi.application.store.endpoint.settings.product.profile.screen

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductTitleScreenTest : AbstractSettingsProductAttributeScreenTest() {
    override fun attributeName() = "title"
}
