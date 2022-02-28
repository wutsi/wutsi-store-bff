package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/settings/store/product/comparable-price")
class SettingsProductComparablePriceScreen(
    catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
) : AbstractSettingsProductAttributeScreen(catalogApi) {
    override fun getAttributeName() = "comparable-price"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_COMPARABLE_PRICE

    override fun getInputWidget(product: Product): WidgetAware {
        val tenant = tenantProvider.get()
        val fmt = DecimalFormat(tenant.monetaryFormat)
        return Column(
            children = listOf(
                Container(
                    padding = 10.0,
                    child = Row(
                        children = listOf(
                            Text(
                                caption = getText("page.settings.store.product.attribute.price"),
                                bold = true
                            ),
                            Container(padding = 10.0),
                            Text(
                                caption = fmt.format(product.price ?: 0)
                            )
                        )
                    )
                ),
                Input(
                    name = "value",
                    value = product.comparablePrice?.toString() ?: "",
                    type = InputType.Number,
                    caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
                    suffix = tenant.currencySymbol,
                )
            )
        )
    }
}
