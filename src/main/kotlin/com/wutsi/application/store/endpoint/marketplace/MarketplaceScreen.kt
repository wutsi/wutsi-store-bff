package com.wutsi.application.store.endpoint.marketplace

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.CategorySummary
import com.wutsi.ecommerce.catalog.dto.SearchCategoryRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
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
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/marketplace")
class MarketplaceScreen(
    private val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                parentId = null,
            )
        ).categories.filter { it.publishedProductCount > 0 }

        return Screen(
            id = Page.MARKETPLACE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.marketplace.app-bar.title"),
            ),
            bottomNavigationBar = bottomNavigationBar(),
            child = Container(
                child = if (categories.isNotEmpty())
                    toCategoyListWidget(categories)
                else
                    Center(
                        Container(
                            padding = 10.0,
                            child = Text(getText("page.marketplace.empty"))
                        )
                    )
            ),
        ).toWidget()
    }

    private fun toCategoyListWidget(categories: List<CategorySummary>): WidgetAware =
        Column(
            crossAxisAlignment = CrossAxisAlignment.start,
            mainAxisAlignment = MainAxisAlignment.start,
            children = listOf(
                Center(
                    Container(
                        padding = 10.0,
                        child = Text(getText("page.marketplace.browse-categories"))
                    )
                ),
                Divider(height = 1.0, color = Theme.COLOR_DIVIDER),
                Flexible(
                    child = ListView(
                        separator = true,
                        separatorColor = Theme.COLOR_DIVIDER,
                        children = categories.map {
                            ListItem(
                                caption = it.title,
                                trailing = Icon(Theme.ICON_CHEVRON_RIGHT),
                                action = gotoUrl(
                                    url = urlBuilder.build("/marketplace/category?id=${it.id}")
                                )
                            )
                        },
                    )
                )
            )
        )
}
