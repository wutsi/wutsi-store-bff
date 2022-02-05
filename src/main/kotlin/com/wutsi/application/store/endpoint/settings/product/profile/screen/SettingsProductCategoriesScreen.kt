package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.SearchCategoryRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/categories")
class SettingsProductCategoriesScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
    private val securityContext: SecurityContext
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val product = catalogApi.getProduct(id).product
        val productCategoryIds = product.categories.map { it.id }
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                accountId = securityContext.currentAccountId()
            )
        ).categories.sortedBy { it.title }

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT_CATEGORIES,
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.attribute.categories"),
            ),
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(
                            bold = true,
                            color = Theme.COLOR_PRIMARY,
                            caption = product.title
                        )
                    ),
                    Container(
                        alignment = Alignment.Center,
                        padding = 10.0,
                        child = Text(getText("page.settings.store.product.attribute.categories.description"))
                    ),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = categories.map {
                                ListItemSwitch(
                                    name = "value",
                                    caption = it.title,
                                    selected = productCategoryIds.contains(it.id),
                                    action = executeCommand(
                                        url = urlBuilder.build("commands/add-product-category?product-id=$id&category-id=${it.id}")
                                    )
                                )
                            }
                        )
                    )
                )
            )
        ).toWidget()
    }
}
