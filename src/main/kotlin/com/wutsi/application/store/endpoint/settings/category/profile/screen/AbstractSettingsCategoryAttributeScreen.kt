package com.wutsi.application.store.endpoint.settings.category.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
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
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.Category
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

abstract class AbstractSettingsCategoryAttributeScreen(
    protected val urlBuilder: URLBuilder,
    protected val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    abstract fun getAttributeName(): String

    abstract fun getPageId(): String

    abstract fun getInputWidget(category: Category): WidgetAware

    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val category = catalogApi.getCategory(id).category
        val name = getAttributeName()
        return Screen(
            id = getPageId(),
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.category.attribute.$name"),
            ),
            child = Form(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(
                            bold = true,
                            color = Theme.COLOR_PRIMARY,
                            caption = category.title
                        )
                    ),
                    Container(
                        alignment = Alignment.Center,
                        padding = 10.0,
                        child = Text(getText("page.settings.store.category.attribute.$name.description"))
                    ),
                    Container(
                        padding = 10.0,
                        child = getInputWidget(category),
                    ),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "submit",
                            type = InputType.Submit,
                            caption = getText("page.settings.store.category.attribute.button.submit"),
                            action = Action(
                                type = ActionType.Command,
                                url = urlBuilder.build("commands/update-category-attribute?id=$id&name=$name")
                            )
                        ),
                    ),
                )
            )
        ).toWidget()
    }
}
