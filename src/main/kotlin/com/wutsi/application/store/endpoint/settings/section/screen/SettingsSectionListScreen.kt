package com.wutsi.application.store.endpoint.settings.section.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.settings.product.list.dto.FilterProductRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
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
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/sections")
class SettingsSectionListScreen(
    private val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestBody request: FilterProductRequest?): Widget {
        val sections = catalogApi.listSections().sections
        return Screen(
            id = Page.SETTINGS_STORE_SECTION_LIST,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.section.list.app-bar.title"),
            ),
            floatingActionButton = Button(
                type = ButtonType.Floatable,
                icon = Theme.ICON_ADD,
                stretched = false,
                iconColor = Theme.COLOR_WHITE,
                action = gotoUrl(
                    url = urlBuilder.build("settings/store/section/add")
                ),
            ),
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.settings.store.section.list.count", arrayOf(sections.size)),
                            alignment = TextAlignment.Center
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = sections.map {
                                ListItem(
                                    caption = it.title,
                                    trailing = Icon(Theme.ICON_CHEVRON_RIGHT),
                                    action = gotoUrl(
                                        url = urlBuilder.build("/settings/store/section/edit?id=${it.id}")
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
