package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

abstract class AbstractSettingsProductAttributeScreen(
    protected val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    abstract fun getAttributeName(): String

    abstract fun getPageId(): String

    abstract fun getInputWidget(product: Product): WidgetAware

    protected open fun showSubmitButton(): Boolean = true

    protected open fun getSubmitAction(id: Long, name: String) = Action(
        type = ActionType.Command,
        url = urlBuilder.build("commands/update-product-attribute?id=$id&name=$name")
    )

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
                children = listOfNotNull(
                    Container(
                        padding = 10.0,
                        child = Text(
                            bold = true,
                            color = Theme.COLOR_PRIMARY,
                            caption = product.title
                        )
                    ),
                    Container(
                        alignment = Alignment.Center,
                        padding = 10.0,
                        child = Text(getText("page.settings.store.product.attribute.$name.description"))
                    ),
                    Container(
                        padding = 10.0,
                        child = getInputWidget(product),
                    ),

                    if (showSubmitButton())
                        Container(
                            padding = 10.0,
                            child = Input(
                                name = "submit",
                                type = InputType.Submit,
                                caption = getText("page.settings.store.product.attribute.button.submit"),
                                action = getSubmitAction(id, name)
                            ),
                        )
                    else
                        null,
                )
            )
        ).toWidget()
    }
}
