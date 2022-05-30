package com.wutsi.application.store.endpoint.marketplace

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchMerchantRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.Category
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.account.entity.AccountSort
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/marketplace")
class MarketplaceScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    @Value("\${wutsi.application.shell-url}") shellUrl: String,
) : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        // Get merchants
        val merchants = catalogApi.searchMerchants(
            request = SearchMerchantRequest(
                withPublishedProducts = true,
                limit = 30,
            )
        ).merchants

        // Get stores
        val stores = if (merchants.isEmpty())
            emptyList()
        else
            accountApi.searchAccount(
                request = SearchAccountRequest(
                    ids = merchants.map { it.accountId },
                    sortBy = AccountSort.NAME.name
                )
            ).accounts

        // Get the categories
        val categories = accountApi.listCategories().categories

        return Screen(
            id = Page.MARKETPLACE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.marketplace.app-bar.title"),
            ),
            bottomNavigationBar = bottomNavigationBar(),
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            child = SingleChildScrollView(
                child = if (stores.isEmpty())
                    Center(
                        Container(
                            padding = 10.0,
                            child = Text(getText("page.marketplace.empty"))
                        )
                    )
                else
                    toStoreListWidget(stores, categories),
            ),
        ).toWidget()
    }

    private fun toStoreListWidget(stores: List<AccountSummary>, categories: List<Category>): WidgetAware {
        val categoryMap = categories.associateBy { it.id }
        val children = mutableListOf<WidgetAware>()
        children.add(
            Center(
                Container(
                    padding = 10.0,
                    child = Text(getText("page.marketplace.stores"), bold = true)
                )
            ),
        )
        children.addAll(
            stores.flatMap {
                listOf(
                    Divider(height = 1.0, color = Theme.COLOR_DIVIDER),
                    ProfileListItem(
                        model = sharedUIMapper.toAccountModel(
                            it,
                            category = if (it.categoryId == null) null else categoryMap[it.categoryId],
                        ),
                        action = gotoUrl(urlBuilder.build(shellUrl, "/profile?id=${it.id}&tab=store")),
                        showAccountType = false,
                        showLocation = true
                    ),
                )
            }
        )
        return Container(
            margin = 5.0,
            background = Theme.COLOR_WHITE,
            borderColor = Theme.COLOR_DIVIDER,
            child = Column(
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisAlignment = MainAxisAlignment.start,
                children = children
            ),
        )
    }
}
