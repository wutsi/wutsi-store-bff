package com.wutsi.application.store.endpoint

import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.dto.PushTrackRequest
import com.wutsi.analytics.tracking.dto.Track
import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.BottomNavigationBar
import com.wutsi.flutter.sdui.BottomNavigationBarItem
import com.wutsi.flutter.sdui.Dialog
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.tenant.dto.Tenant
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import java.net.URLEncoder
import java.text.DecimalFormat
import javax.servlet.http.HttpServletRequest

abstract class AbstractEndpoint {
    @Autowired
    protected lateinit var messages: MessageSource

    @Autowired
    protected lateinit var securityContext: SecurityContext

    @Autowired
    protected lateinit var trackingApi: WutsiTrackingApi

    @Autowired
    protected lateinit var urlBuilder: URLBuilder

    @Autowired
    private lateinit var tracingContext: TracingContext

    @Value("\${wutsi.application.shell-url}")
    protected lateinit var shellUrl: String

    @Value("\${wutsi.application.cash-url}")
    protected lateinit var cashUrl: String

    protected fun track(
        correlationId: String,
        page: String,
        event: EventType,
        productId: Long?,
        merchantId: Long,
        value: Double?,
        request: HttpServletRequest
    ) {
        try {
            trackingApi.push(
                request = PushTrackRequest(
                    track = Track(
                        time = System.currentTimeMillis(),
                        tenantId = tracingContext.tenantId(),
                        deviceId = tracingContext.deviceId(),
                        productId = productId?.toString(),
                        accountId = securityContext.currentAccountId().toString(),
                        merchantId = merchantId.toString(),
                        correlationId = correlationId,
                        value = value,
                        page = page,
                        event = event.name,
                        ua = request.getHeader("User-Agent"),
                        ip = request.getHeader("X-Forwarded-For") ?: request.remoteAddr,
                        referer = request.getHeader("Referer")
                    )
                )
            )
        } catch (ex: Exception) {
            LoggerFactory.getLogger(this::class.java)
                .warn("Unable to track $event on $page for Product#$productId", ex)
        }
    }

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
                    url = urlBuilder.build(cashUrl, "history")
                )
            ),
            BottomNavigationBarItem(
                icon = Theme.ICON_SETTINGS,
                caption = getText("page.home.bottom-nav-bar.settings"),
                action = Action(
                    type = ActionType.Route,
                    url = urlBuilder.build(shellUrl, "settings")
                )
            ),
        )
    )

    protected fun formatDeliveryTime(value: Int): String =
        try {
            getText("shipping.delivery-time.$value")
        } catch (ex: Exception) {
            val days = value / 12
            if (days < 1)
                getText("shipping.delivery-time.less-than-1d")
            else
                getText("shipping.delivery-time.n-days")
        }

    protected fun formatRate(rate: Double?, tenant: Tenant): String =
        if (rate == null || rate == 0.0)
            getText("label.free")
        else
            DecimalFormat(tenant.monetaryFormat).format(rate)
}
