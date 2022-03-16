package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/numeric-file-url")
class SettingsProductFileScreen(
    catalogApi: WutsiCatalogApi
) : AbstractSettingsProductAttributeScreen(catalogApi) {
    override fun getAttributeName() = "numeric-file-url"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_FILE

    override fun getInputWidget(product: Product): WidgetAware = Input(
        name = "value",
        type = InputType.File,
        caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
        uploadUrl = urlBuilder.build("commands/upload-product-file?product-id=${product.id}"),
    )
}
