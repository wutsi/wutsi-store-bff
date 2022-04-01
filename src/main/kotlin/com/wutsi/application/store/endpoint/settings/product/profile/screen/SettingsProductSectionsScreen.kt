package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SectionSummary
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/sections")
class SettingsProductSectionsScreen(
    private val catalogApi: WutsiCatalogApi,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val product = catalogApi.getProduct(id).product
        val sections = catalogApi.listSections().sections
        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT_SECTION,
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.attribute.sections"),
            ),
            child = Form(
                children = listOfNotNull(
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
                        child = Text(getText("page.settings.store.product.attribute.sections.description"))
                    ),
                    Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = sections.map {
                                ListItemSwitch(
                                    name = "value",
                                    caption = it.title,
                                    selected = isSelected(product.sections, it),
                                    action = executeCommand("commands/toggle-product-section?id=$id&section-id=${it.id}")
                                )
                            }
                        )
                    ),
                )
            )
        ).toWidget()
    }

    private fun isSelected(sections: List<SectionSummary>, section: SectionSummary): Boolean =
        sections.find { it.id == section.id } != null
}
