package com.wutsi.application.store.endpoint.catalog.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.ProductCard
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.ProductSummary
import com.wutsi.platform.catalog.dto.SearchProductRequest
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog")
class CatalogScreen(
    private val urlBuilder: URLBuilder,
    private val accountApi: WutsiAccountApi,
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
    private val securityContext: SecurityContext,
    private val sharedUIMapper: SharedUIMapper,

    @Value("\${wutsi.application.shell-url}") private val shellUrl: String,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam(required = false) id: Long? = null): Widget {
        val account = id?.let { accountApi.getAccount(id).account } ?: securityContext.currentAccount()
        val tenant = tenantProvider.get()
        val products = catalogApi.searchProduct(
            SearchProductRequest(
                accountId = account.id,
                limit = 100
            )
        ).products
        val rows = toRows(products, 2)

        return Screen(
            id = Page.CATALOG,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.catalog.app-bar.title"),
                actions = listOf(
                    IconButton(
                        icon = Theme.ICON_SETTINGS,
                        action = Action(
                            type = ActionType.Route,
                            url = urlBuilder.build(shellUrl, "settings")
                        )
                    )
                ),
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    ProfileListItem(model = sharedUIMapper.toAccountModel(account)),
                    Flexible(
                        child = ListView(
                            children = rows.map {
                                Container(
                                    child = Row(
                                        children = thumbnails(it, tenant),
                                    )
                                )
                            }
                        ),
                    ),
                )
            )
        ).toWidget()
    }

    private fun toRows(products: List<ProductSummary>, size: Int): List<List<ProductSummary>> {
        val rows = mutableListOf<List<ProductSummary>>()
        var cur = mutableListOf<ProductSummary>()
        products.forEach {
            cur.add(it)
            if (cur.size == size) {
                rows.add(cur)
                cur = mutableListOf()
            }
        }
        if (cur.isNotEmpty())
            rows.add(cur)
        return rows
    }

    private fun thumbnails(products: List<ProductSummary>, tenant: Tenant): List<WidgetAware> =
        products.map {
            Flexible(
                child = Container(
                    alignment = Alignment.TopCenter,
                    child = ProductCard(
                        model = sharedUIMapper.toProductModel(it, tenant),
                        action = gotoUrl(urlBuilder.build("/product?id=${it.id}"))
                    )
                )
            )
        }
}
