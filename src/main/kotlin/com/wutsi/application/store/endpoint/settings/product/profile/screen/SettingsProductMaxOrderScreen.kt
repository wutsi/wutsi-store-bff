package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.Product
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/max-order")
class SettingsProductMaxOrderScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi,
) : AbstractSettingsProductAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "max-order"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_MAX_ORDER

    override fun getInputWidget(product: Product): WidgetAware {
        return Input(
            name = "value",
            value = product.maxOrder?.toString() ?: "",
            type = InputType.Number,
            caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
        )
    }
}
