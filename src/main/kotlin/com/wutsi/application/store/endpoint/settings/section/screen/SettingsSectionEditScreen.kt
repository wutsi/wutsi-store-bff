package com.wutsi.application.store.endpoint.settings.section.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/section/edit")
class SettingsSectionEditScreen(
    private val catalogApi: WutsiCatalogApi
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val section = catalogApi.getSection(id).section
        return Screen(
            id = Page.SETTINGS_STORE_SECTION_EDIT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.section.edit.app-bar.title")
            ),
            child = SingleChildScrollView(
                child = Form(
                    children = listOf(
                        Container(
                            padding = 10.0,
                            child = Input(
                                name = "title",
                                maxLength = 30,
                                caption = getText("page.settings.store.section.edit.title"),
                                required = true,
                                value = section.title
                            )
                        ),
                        Container(
                            padding = 10.0,
                            child = Input(
                                name = "submit",
                                type = InputType.Submit,
                                caption = getText("page.settings.store.section.add.button.submit"),
                                action = executeCommand(
                                    url = urlBuilder.build("commands/update-section?id=$id&sort-order=${section.sortOrder}")
                                )
                            )
                        )
                    ),
                )
            )
        ).toWidget()
    }
}
