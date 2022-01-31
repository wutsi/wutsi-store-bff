package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.TenantProvider
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
@RequestMapping("/settings/store/product/price")
class SettingsProductPriceScreen(
    urlBuilder: URLBuilder,
    catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
) : AbstractSettingsProductAttributeScreen(urlBuilder, catalogApi) {
    override fun getAttributeName() = "price"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_PRICE

    override fun getInputWidget(product: Product): WidgetAware {
        val tenant = tenantProvider.get()
        return Input(
            name = "value",
            value = product.price?.toString() ?: "",
            type = InputType.Number,
            caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
            suffix = tenant.currencySymbol
        )
    }
}
