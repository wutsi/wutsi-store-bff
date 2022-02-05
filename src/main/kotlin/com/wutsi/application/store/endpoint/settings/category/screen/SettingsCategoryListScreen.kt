package com.wutsi.application.store.endpoint.settings.category.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.SearchCategoryRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/categories")
class SettingsCategoryListScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
    private val securityContext: SecurityContext,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                accountId = securityContext.currentAccountId(),
            )
        ).categories.sortedBy { it.title }

        return Screen(
            id = Page.SETTINGS_STORE_CATEGORY_LIST,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.category.list.app-bar.title"),
            ),
            floatingActionButton = Button(
                type = ButtonType.Floatable,
                icon = Theme.ICON_ADD,
                stretched = false,
                iconColor = Theme.COLOR_WHITE,
                action = gotoUrl(
                    url = urlBuilder.build("settings/store/category/add")
                ),
            ),
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        alignment = Alignment.CenterLeft,
                        child = Text(
                            caption = getText("page.settings.store.category.list.count", arrayOf(categories.size)),
                            alignment = TextAlignment.Left
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER, height = 2.0),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = categories.map {
                                ListItem(
                                    caption = it.title,
                                    action = gotoUrl(
                                        urlBuilder.build("settings/store/category?id=${it.id}")
                                    )
                                )
                            }
                        )
                    )
                )
            ),
        ).toWidget()
    }
}
