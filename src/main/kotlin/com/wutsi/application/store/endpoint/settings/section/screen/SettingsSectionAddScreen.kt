package com.wutsi.application.store.endpoint.settings.section.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/section/add")
class SettingsSectionAddScreen : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        return Screen(
            id = Page.SETTINGS_STORE_SECTION_ADD,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.section.add.app-bar.title")
            ),
            child = SingleChildScrollView(
                child = Form(
                    children = listOf(
                        Container(
                            padding = 10.0,
                            child = Input(
                                name = "title",
                                maxLength = 30,
                                caption = getText("page.settings.store.section.add.title"),
                                required = true
                            )
                        ),
                        Container(
                            padding = 10.0,
                            child = Input(
                                name = "submit",
                                type = InputType.Submit,
                                caption = getText("page.settings.store.section.add.button.submit"),
                                action = Action(
                                    type = ActionType.Command,
                                    url = urlBuilder.build("commands/create-section"),
                                )
                            )
                        )
                    ),
                )
            )
        ).toWidget()
    }
}
