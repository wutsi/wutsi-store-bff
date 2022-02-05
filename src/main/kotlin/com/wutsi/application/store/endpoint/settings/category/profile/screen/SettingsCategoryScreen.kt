package com.wutsi.application.store.endpoint.settings.category.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.catalog.WutsiCatalogApi
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/category")
class SettingsCategoryScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val category = catalogApi.getCategory(id).category

        return Screen(
            id = Page.SETTINGS_STORE_CATEGORY,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.category.app-bar.title"),
            ),
            child = Container(
                child = Column(
                    children = listOf(
                        Flexible(
                            flex = 10,
                            child = ListView(
                                separatorColor = Theme.COLOR_DIVIDER,
                                separator = true,
                                children = listOfNotNull(
                                    item(
                                        "page.settings.store.category.attribute.title",
                                        category.title,
                                        urlBuilder.build("/settings/store/category/title?id=$id")
                                    ),
                                    ListItemSwitch(
                                        caption = getText("page.settings.store.category.attribute.visible"),
                                        subCaption = getText("page.settings.store.category.attribute.visible.description"),
                                        name = "value",
                                        selected = category.visible,
                                        action = Action(
                                            type = ActionType.Command,
                                            url = urlBuilder.build("commands/update-category-attribute?id=$id&name=visible")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }

    private fun item(caption: String, value: String?, url: String) = ListItem(
        caption = getText(caption),
        subCaption = value,
        trailing = Icon(
            code = Theme.ICON_EDIT,
            size = 24.0,
            color = Theme.COLOR_BLACK
        ),
        action = Action(
            type = ActionType.Route,
            url = url
        )
    )
}
