package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.WidgetAware
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/title")
class SettingsProductTitleScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi
) : AbstractSettingsProductAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "title"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_TITLE

    override fun getInputWidget(product: Product): WidgetAware = Input(
        name = "value",
        value = product.title,
        maxLength = 100,
        required = true,
        caption = getText("page.settings.store.product.attribute.${getAttributeName()}")
    )
}
