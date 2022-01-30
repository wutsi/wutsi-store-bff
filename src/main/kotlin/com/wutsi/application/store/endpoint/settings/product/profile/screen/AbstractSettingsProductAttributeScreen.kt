package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.Product
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

abstract class AbstractSettingsProductAttributeScreen(
    protected val urlBuilder: URLBuilder,
    protected val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    @Value("\${wutsi.application.default-picture-url}")
    private lateinit var defaultPictureUrl: String

    abstract fun getAttributeName(): String

    abstract fun getPageId(): String

    abstract fun getInputWidget(product: Product): WidgetAware

    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val product = catalogApi.getProduct(id).product
        val name = getAttributeName()
        return Screen(
            id = getPageId(),
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.attribute.$name"),
            ),
            child = Form(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Image(
                            width = 150.0,
                            height = 150.0,
                            url = product.thumbnail?.url ?: defaultPictureUrl
                        )
                    ),
                    Container(
                        alignment = Alignment.Center,
                        child = Text(getText("page.settings.store.product.attribute.$name.description"))
                    ),
                    Container(
                        padding = 10.0,
                        child = getInputWidget(product),
                    ),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "submit",
                            type = InputType.Submit,
                            caption = getText("page.settings.store.product.attribute.button.submit"),
                            action = Action(
                                type = ActionType.Command,
                                url = urlBuilder.build("commands/update-product-attribute?id=$id&name=$name")
                            )
                        ),
                    ),
                )
            )
        ).toWidget()
    }
}
