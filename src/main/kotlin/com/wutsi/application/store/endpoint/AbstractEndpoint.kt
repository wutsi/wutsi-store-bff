package com.wutsi.application.store.endpoint

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.BottomNavigationBar
import com.wutsi.flutter.sdui.BottomNavigationBarItem
import com.wutsi.flutter.sdui.Dialog
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.DialogType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.net.URLEncoder

abstract class AbstractEndpoint {
    @Autowired
    protected lateinit var messages: MessageSource

    @Autowired
    protected lateinit var securityContext: SecurityContext

    @Autowired
    protected lateinit var urlBuilder: URLBuilder

    @Value("\${wutsi.application.shell-url}")
    protected lateinit var shellUrl: String

    protected fun gotoUrl(
        url: String,
        replacement: Boolean? = null,
        parameters: Map<String, String>? = null
    ) = Action(
        type = ActionType.Route,
        url = url,
        replacement = replacement,
        parameters = parameters
    )

    protected fun gotoPreviousScreen() = Action(
        type = ActionType.Route,
        url = "route:/..",
    )

    protected fun executeCommand(url: String) = Action(
        type = ActionType.Command,
        url = url
    )

    protected fun showError(message: String) = Action(
        type = ActionType.Prompt,
        prompt = Dialog(
            type = DialogType.Error,
            message = message,
        ).toWidget()
    )

    protected fun getText(key: String, args: Array<Any?> = emptyArray()) =
        messages.getMessage(key, args, LocaleContextHolder.getLocale()) ?: key

    protected fun encodeURLParam(text: String?): String =
        text?.let { URLEncoder.encode(it, "utf-8") } ?: ""

    protected fun bottomNavigationBar() = BottomNavigationBar(
        background = Theme.COLOR_PRIMARY,
        selectedItemColor = Theme.COLOR_WHITE,
        unselectedItemColor = Theme.COLOR_WHITE,
        items = listOf(
            BottomNavigationBarItem(
                icon = Theme.ICON_HOME,
                caption = getText("page.home.bottom-nav-bar.home"),
                action = Action(
                    type = ActionType.Route,
                    url = "route:/~"
                )
            ),
            BottomNavigationBarItem(
                icon = Theme.ICON_PERSON,
                caption = getText("page.home.bottom-nav-bar.me"),
                action = Action(
                    type = ActionType.Route,
                    url = urlBuilder.build(shellUrl, "profile?id=${securityContext.currentAccountId()}"),
                )
            ),
            BottomNavigationBarItem(
                icon = Theme.ICON_HISTORY,
                caption = getText("page.home.bottom-nav-bar.transactions"),
                action = Action(
                    type = ActionType.Route,
                    url = urlBuilder.build("history")
                )
            ),
            BottomNavigationBarItem(
                icon = Theme.ICON_SETTINGS,
                caption = getText("page.home.bottom-nav-bar.settings"),
                action = Action(
                    type = ActionType.Route,
                    url = urlBuilder.build("settings")
                )
            ),
        )
    )
}
