package com.wutsi.application.store.endpoint.settings.category.profile.screen

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.Category
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/category/title")
class SettingsCategoryTitleScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi
) : AbstractSettingsCategoryAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "title"

    override fun getPageId() = Page.SETTINGS_STORE_CATEGORY_TITLE

    override fun getInputWidget(category: Category): WidgetAware = Input(
        name = "value",
        value = category.title,
        maxLength = 100,
        required = true,
        caption = getText("page.settings.store.product.attribute.${getAttributeName()}")
    )
}
