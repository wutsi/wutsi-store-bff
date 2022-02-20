package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/quantity")
class SettingsProductQuantityScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi,
) : AbstractSettingsProductAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "quantity"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_QUANTIY

    override fun getInputWidget(product: Product): WidgetAware {
        return Input(
            name = "value",
            value = product.quantity.toString(),
            type = InputType.Number,
            caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
        )
    }
}
