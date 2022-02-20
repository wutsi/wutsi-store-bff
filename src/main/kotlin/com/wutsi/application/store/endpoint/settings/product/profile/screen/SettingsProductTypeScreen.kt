package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.Product
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/type")
class SettingsProductTypeScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi,
) : AbstractSettingsProductAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "type"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_TYPE

    override fun getInputWidget(product: Product): WidgetAware {
        return DropdownButton(
            name = "value",
            hint = getText("page.settings.store.product.editor.type"),
            value = product.type,
            required = true,
            children = listOf(
                DropdownMenuItem(
                    caption = getText("product.type.PHYSICAL"),
                    value = "PHYSICAL",
                ),
                DropdownMenuItem(
                    caption = getText("product.type.NUMERIC"),
                    value = "NUMERIC",
                ),
            )
        )
    }
}
