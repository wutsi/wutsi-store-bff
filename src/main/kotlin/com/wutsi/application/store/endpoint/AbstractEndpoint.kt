package com.wutsi.application.store.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.dto.PushTrackRequest
import com.wutsi.analytics.tracking.dto.Track
import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.BottomNavigationBarWidget
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.order.error.ErrorURN
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Dialog
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.tenant.dto.Tenant
import feign.FeignException
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

    @Autowired
    protected lateinit var togglesProvider: TogglesProvider

    @Autowired
    protected lateinit var cartApi: WutsiCartApi

    @Value("\${wutsi.application.shell-url}")
    protected lateinit var shellUrl: String

    @Value("\${wutsi.application.cash-url}")
    protected lateinit var cashUrl: String

    @Autowired
    protected lateinit var sharedUIMapper: SharedUIMapper

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    protected fun getErrorText(ex: FeignException): String {
        try {
            val response = objectMapper.readValue(ex.contentUTF8(), ErrorResponse::class.java)
            val code = response.error.code
            if (code == ErrorURN.PRODUCT_AVAILABILITY_ERROR.urn) {
                return getText("error.order.PRODUCT_AVAILABILITY_ERROR")
            } else if (code == com.wutsi.platform.payment.error.ErrorURN.TRANSACTION_FAILED.urn) {
                val downstreamCode = response.error.downstreamCode
                return getTransactionErrorText(downstreamCode)
            }
        } catch (ex: Exception) {
        }

        return getText("error.unexpected")
    }

    protected fun getTransactionErrorText(errorCode: String?): String =
        try {
            getText("error.payment.$errorCode")
        } catch (ex: Exception) {
            getText("error.payment")
        }

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

    protected fun gotoHomeScreen() = Action(
        type = ActionType.Route,
        url = "route:/~",
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

    protected fun bottomNavigationBar() = BottomNavigationBarWidget(
        model = sharedUIMapper.toBottomNavigationBarModel(
            shellUrl = shellUrl,
            cashUrl = cashUrl,
            togglesProvider = togglesProvider,
            urlBuilder = urlBuilder
        )
    ).toBottomNavigationBar()

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

    protected fun getCart(merchant: Account): Cart? =
        if (merchant.business && togglesProvider.isCartEnabled() && merchant.hasStore)
            try {
                cartApi.getCart(merchant.id).cart
            } catch (ex: Exception) {
                null
            }
        else
            null
}
