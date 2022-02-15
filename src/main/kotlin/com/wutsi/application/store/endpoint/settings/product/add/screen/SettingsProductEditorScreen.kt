package com.wutsi.application.store.endpoint.settings.product.add.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.SearchCategoryRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/editor")
class SettingsProductEditorScreen(
    private val urlBuilder: URLBuilder,
    private val tenantProvider: TenantProvider,
    private val catalogApi: WutsiCatalogApi
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam(name = "category-id") categoryId: Long): Widget {
        val tenant = tenantProvider.get()
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                parentId = categoryId
            )
        ).categories

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT_EDITOR,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.editor.app-bar.title")
            ),
            child = Form(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "title",
                            maxLength = 100,
                            caption = getText("page.settings.store.product.editor.title"),
                            required = true
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = DropdownButton(
                            name = "subCategoryId",
                            value = null,
                            required = true,
                            children = categories
                                .sortedBy { it.title }
                                .map {
                                    DropdownMenuItem(
                                        caption = it.title,
                                        value = it.id.toString(),
                                    )
                                }
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "summary",
                            maxLength = 160,
                            caption = getText("page.settings.store.product.editor.summary")
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "price",
                            maxLength = 10,
                            caption = getText("page.settings.store.product.editor.price"),
                            type = InputType.Number,
                            suffix = tenant.currencySymbol
                        )
                    ),
                    Container(
                        padding = 10.0,
                        child = Input(
                            name = "submit",
                            type = InputType.Submit,
                            caption = getText("page.settings.store.product.editor.button.submit"),
                            action = Action(
                                type = ActionType.Command,
                                url = urlBuilder.build("commands/add-product"),
                                parameters = mapOf(
                                    "category-id" to categoryId.toString()
                                )
                            )
                        )
                    )
                ),
            )
        ).toWidget()
    }
}
