package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.flutter.sdui.enums.TextOverflow
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/numeric-file-url")
class SettingsProductFileScreen(
    catalogApi: WutsiCatalogApi
) : AbstractSettingsProductAttributeScreen(catalogApi) {
    override fun getAttributeName() = "numeric-file-url"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_FILE

    override fun showSubmitButton() = false

    override fun getInputWidget(product: Product): WidgetAware = Column(
        children = listOfNotNull(
            getFileName(product.numericFileUrl)?.let {
                Container(
                    alignment = Alignment.Center,
                    child = Text(
                        it,
                        size = Theme.TEXT_SIZE_SMALL,
                        alignment = TextAlignment.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Clip
                    ),
                )
            },
            Input(
                name = "file",
                type = InputType.File,
                caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
                uploadUrl = urlBuilder.build("commands/upload-product-file?product-id=${product.id}"),
            )
        )
    )
}
