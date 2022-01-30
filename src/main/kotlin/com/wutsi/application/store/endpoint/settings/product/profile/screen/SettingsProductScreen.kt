package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.platform.catalog.WutsiCatalogApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/settings/store/product")
class SettingsProductScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,

    @Value("\${wutsi.application.default-picture-url}") private val defaultPictureUrl: String
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val product = catalogApi.getProduct(id).product
        val tenant = tenantProvider.get()
        val price = product.price?.let { DecimalFormat(tenant.monetaryFormat).format(it) }

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.app-bar.title"),
            ),
            child = Container(
                child = Flexible(
                    child = ListView(
                        separatorColor = Theme.COLOR_DIVIDER,
                        separator = true,
                        children = listOf(
                            Container(
                                alignment = Alignment.Center,
                                child = Column(
                                    mainAxisSize = MainAxisSize.min,
                                    children = listOf(
                                        Image(
                                            width = 150.0,
                                            height = 150.0,
                                            url = product.thumbnail?.url ?: defaultPictureUrl
                                        ),
                                        Button(
                                            type = ButtonType.Text,
                                            padding = 10.0,
                                            caption = getText("page.settings.store.product.button.upload-picture"),
                                            action = gotoUrl(urlBuilder.build("/settings/store/picture?id=$id"))
                                        )
                                    )
                                )
                            ),
                            item(
                                "page.settings.store.product.attribute.title",
                                product.title,
                                urlBuilder.build("/settings/store/product/title?id=$id")
                            ),
                            item(
                                "page.settings.store.product.attribute.price",
                                price,
                                urlBuilder.build("/settings/store/product/price?id=$id")
                            ),
                            item(
                                "page.settings.store.product.attribute.summary",
                                product.summary,
                                urlBuilder.build("/settings/store/product/summary?id=$id")
                            ),
                            item(
                                "page.settings.store.product.attribute.description",
                                description(product.description),
                                urlBuilder.build("/settings/store/product/description?id=$id")
                            ),
                            ListItemSwitch(
                                caption = getText("page.settings.store.product.attribute.visible"),
                                subCaption = getText("page.settings.store.product.attribute.visible.description"),
                                name = "value",
                                selected = product.visible,
                                action = Action(
                                    type = ActionType.Command,
                                    url = urlBuilder.build("commands/update-product-attribute?id=$id&name=visible")
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }

    private fun item(caption: String, value: String?, url: String) = ListItem(
        caption = getText(caption),
        subCaption = value,
        trailing = Icon(
            code = Theme.ICON_EDIT,
            size = 24.0,
            color = Theme.COLOR_BLACK
        ),
        action = Action(
            type = ActionType.Route,
            url = url
        )
    )

    private fun description(value: String?): String? =
        if (value == null)
            null
        else if (value.length < 160)
            value
        else
            value.substring(0, 160) + "..."
}
