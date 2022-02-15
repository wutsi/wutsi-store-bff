package com.wutsi.application.store.endpoint.settings.product.add.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.SearchCategoryRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/add")
class SettingsProductAddScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                parentId = null
            )
        ).categories

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT_ADD,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.add.app-bar.title")
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.settings.store.product.add.message"),
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    Flexible(
                        child = ListView(
                            separatorColor = Theme.COLOR_DIVIDER,
                            separator = true,
                            children = categories.map {
                                ListItem(
                                    caption = it.title,
                                    trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
                                    action = gotoUrl(
                                        urlBuilder.build("settings/store/product/editor?category-id=${it.id}")
                                    )
                                )
                            }
                        )
                    ),
                ),
            )
        ).toWidget()
    }
}
