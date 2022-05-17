package com.wutsi.application.store.endpoint.settings.product.list.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.ProductListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.settings.product.list.dto.FilterProductRequest
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.catalog.entity.ProductStatus
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Flexible
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
@RequestMapping("/settings/store/products")
class SettingsProductListScreen(
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    companion object {
        const val DEFAULT_CATEGORY_ID = -1L
    }

    @PostMapping
    fun index(@RequestBody request: FilterProductRequest?): Widget {
        val tenant = tenantProvider.get()
        val accountId = securityContext.currentAccountId()
        val status = request?.status ?: ProductStatus.PUBLISHED.name
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                accountId = accountId,
                status = status,
                limit = 100
            )
        ).products

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT_LIST,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.list.app-bar.title"),
            ),
            floatingActionButton = Button(
                type = ButtonType.Floatable,
                icon = Theme.ICON_ADD,
                stretched = false,
                iconColor = Theme.COLOR_WHITE,
                action = gotoUrl(
                    url = urlBuilder.build("settings/store/product/add")
                ),
            ),
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = DropdownButton(
                            name = "status",
                            value = status,
                            children = listOf(
                                DropdownMenuItem(
                                    caption = getText("product.status.PUBLISHED"),
                                    value = ProductStatus.PUBLISHED.name
                                ),
                                DropdownMenuItem(
                                    caption = getText("product.status.DRAFT"),
                                    value = ProductStatus.DRAFT.name
                                )
                            ),
                            action = gotoUrl(
                                url = urlBuilder.build("/settings/store/products"),
                                replacement = true
                            )
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.settings.store.product.list.count", arrayOf(products.size)),
                            alignment = TextAlignment.Center
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = products.map {
                                ProductListItem(
                                    model = sharedUIMapper.toProductModel(it, tenant),
                                    action = gotoUrl(
                                        url = urlBuilder.build("/settings/store/product?id=${it.id}")
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
