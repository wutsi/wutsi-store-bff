package com.wutsi.application.store.endpoint.settings.statistics.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Metrics
import com.wutsi.ecommerce.catalog.dto.SearchMerchantRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.MoneyText
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("settings/store/statistics")
class SettingsStatisticsScreen(
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val merchants = catalogApi.searchMerchants(
            request = SearchMerchantRequest(
                accountIds = listOf(securityContext.currentAccountId())
            )
        ).merchants
        val metrics = if (merchants.isEmpty())
            Metrics()
        else
            catalogApi.getMerchant(merchants[0].id).merchant.overallMetrics

        return Screen(
            id = Page.SETTINGS_STORE_STATISTICS,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.statistics.app-bar.title"),
            ),

            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Flexible(
                        child = toStatWidget(
                            "page.settings.store.statistics.sales",
                            metrics.totalSales.toDouble(),
                            tenant,
                            money = true,
                            color = Theme.COLOR_PRIMARY,
                            valueSize = 40.0
                        )
                    ),
                    Flexible(
                        child = toStatWidget(
                            "page.settings.store.statistics.views",
                            metrics.totalViews.toDouble(),
                            tenant,
                        )
                    ),
                    Flexible(
                        child = toStatWidget(
                            "page.settings.store.statistics.orders",
                            metrics.totalOrders.toDouble(),
                            tenant,
                        )
                    ),
                    Flexible(
                        child = toStatWidget(
                            "page.settings.store.statistics.conversion",
                            metrics.conversion,
                            tenant,
                            percent = true,
                        )
                    ),
                )
            )
        ).toWidget()
    }

    private fun toStatWidget(
        name: String,
        value: Double,
        tenant: Tenant,
        money: Boolean = false,
        percent: Boolean = false,
        color: String = Theme.COLOR_BLACK,
        valueSize: Double = 30.0,
    ): WidgetAware =
        Container(
            padding = 10.0,
            margin = 10.0,
            border = 1.0,
            borderColor = Theme.COLOR_DIVIDER,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.center,
                crossAxisAlignment = CrossAxisAlignment.center,
                children = listOf(
                    if (money)
                        MoneyText(
                            value = value,
                            currency = tenant.currencySymbol,
                            color = color,
                            numberFormat = tenant.numberFormat,
                            valueFontSize = valueSize,
                        )
                    else
                        Text(
                            caption = if (percent)
                                DecimalFormat("0.00%").format(value)
                            else
                                DecimalFormat(tenant.numberFormat).format(value),
                            size = valueSize,
                            color = color
                        ),
                    Center(
                        child = Text(
                            caption = getText(name).uppercase(),
                            size = Theme.TEXT_SIZE_SMALL,
                            alignment = TextAlignment.Center,
                        )
                    )
                )
            )
        )
}
