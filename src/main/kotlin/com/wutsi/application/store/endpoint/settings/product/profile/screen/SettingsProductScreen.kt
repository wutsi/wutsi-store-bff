package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.catalog.entity.ProductStatus
import com.wutsi.ecommerce.catalog.entity.ProductType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Dialog
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.Axis
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.ImageSource
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import java.text.DecimalFormat

@RestController
@RequestMapping("/settings/store/product")
class SettingsProductScreen(
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,

    @Value("\${wutsi.store.pictures.max-width}") private val pictureMaxWidth: Int,
    @Value("\${wutsi.store.pictures.max-width}") private val pictureMaxHeight: Int,
    @Value("\${wutsi.store.pictures.max-per-product}") private val maxPicturesPerProduct: Int,
) : AbstractQuery() {
    companion object {
        const val IMAGE_WIDTH = 150.0
        const val IMAGE_HEIGHT = 150.0
        const val IMAGE_PADDING = 2.0
    }

    @PostMapping
    fun index(
        @RequestParam id: Long,
        @RequestParam(required = false) errors: Array<String>? = null
    ): Widget {
        val product = catalogApi.getProduct(id).product
        val tenant = tenantProvider.get()
        val price = product.price?.let { DecimalFormat(tenant.monetaryFormat).format(it) }
        val comparablePrice = product.comparablePrice?.let { DecimalFormat(tenant.monetaryFormat).format(it) }

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.app-bar.title"),
            ),
            child = Container(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = listOfNotNull(
                        Container(
                            padding = 10.0,
                            height = IMAGE_HEIGHT + 2 * (10.0 + IMAGE_PADDING),
                            child = toPictureListWidget(product),
                        ),

                        if (product.status == ProductStatus.DRAFT.name)
                            Center(
                                child = Container(
                                    padding = 10.0,
                                    margin = 10.0,
                                    background = Theme.COLOR_WARNING_LIGHT,
                                    child = Text(
                                        caption = getText("product.status.${product.status}"),
                                        color = Theme.COLOR_WARNING,
                                    )
                                )
                            )
                        else
                            null,

                        if (errors != null)
                            toErrorWidget(errors)
                        else
                            null,

                        Divider(color = Theme.COLOR_DIVIDER, height = 1.0),
                        Flexible(
                            flex = 10,
                            child = ListView(
                                separatorColor = Theme.COLOR_DIVIDER,
                                separator = true,
                                children = listOfNotNull(
                                    ListItemSwitch(
                                        caption = getText("page.settings.store.product.attribute.published"),
                                        name = "value",
                                        selected = product.status == ProductStatus.PUBLISHED.name,
                                        action = Action(
                                            type = ActionType.Command,
                                            url = urlBuilder.build("commands/publish-product?id=$id")
                                        )
                                    ),
                                    toListItemWidget(
                                        "page.settings.store.product.attribute.title",
                                        product.title,
                                        urlBuilder.build("/settings/store/product/title?id=$id")
                                    ),
                                    if (togglesProvider.isDigitalProductEnabled())
                                        toListItemWidget(
                                            "page.settings.store.product.attribute.type",
                                            getText("product.type." + product.type),
                                            urlBuilder.build("/settings/store/product/type?id=$id")
                                        )
                                    else
                                        null,

                                    if (togglesProvider.isDigitalProductEnabled() && product.type == ProductType.NUMERIC.name)
                                        toListItemWidget(
                                            "page.settings.store.product.attribute.numeric-file-url",
                                            product.numericFileUrl?.let { getFileName(it) },
                                            urlBuilder.build("/settings/store/product/numeric-file-url?id=$id")
                                        )
                                    else
                                        null,

                                    toListItemWidget(
                                        "page.settings.store.product.attribute.sections",
                                        product.sections.map { it.title }.joinToString(", "),
                                        urlBuilder.build("/settings/store/product/sections?id=$id")
                                    ),

                                    toListItemWidget(
                                        "page.settings.store.product.attribute.sub-category-id",
                                        "${product.category.title} > ${product.subCategory.title}",
                                        urlBuilder.build("/settings/store/product/sub-category-id?id=$id")
                                    ),
                                    toListItemWidget(
                                        "page.settings.store.product.attribute.price",
                                        price,
                                        urlBuilder.build("/settings/store/product/price?id=$id")
                                    ),

                                    if (product.price != null)
                                        toListItemWidget(
                                            "page.settings.store.product.attribute.comparable-price",
                                            comparablePrice,
                                            urlBuilder.build("/settings/store/product/comparable-price?id=$id")
                                        )
                                    else
                                        null,

                                    toListItemWidget(
                                        "page.settings.store.product.attribute.summary",
                                        product.summary,
                                        urlBuilder.build("/settings/store/product/summary?id=$id")
                                    ),
                                    toListItemWidget(
                                        "page.settings.store.product.attribute.quantity",
                                        product.quantity.toString(),
                                        urlBuilder.build("/settings/store/product/quantity?id=$id")
                                    ),
                                    toListItemWidget(
                                        "page.settings.store.product.attribute.max-order",
                                        product.maxOrder?.toString(),
                                        urlBuilder.build("/settings/store/product/max-order?id=$id")
                                    ),
                                    toListItemWidget(
                                        "page.settings.store.product.attribute.description",
                                        description(product.description),
                                        urlBuilder.build("/settings/store/product/description?id=$id")
                                    ),
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }

    private fun toErrorWidget(errors: Array<String>): WidgetAware =
        Center(
            child = Container(
                padding = 10.0,
                background = Theme.COLOR_DANGER_LIGHT,
                child = Column(
                    children = getErrorTexts(errors).map {
                        Text(
                            caption = it,
                            color = Theme.COLOR_DANGER
                        )
                    }
                )
            )
        )

    private fun toListItemWidget(caption: String, value: String?, url: String) = ListItem(
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

    private fun toPictureListWidget(product: Product): WidgetAware {
        val images = mutableListOf<WidgetAware>()

        // Thumbnail as 1st image
        if (product.thumbnail != null)
            images.add(toPictureWidget(product, product.thumbnail!!))

        // Other pictures
        images.addAll(
            product.pictures
                .filter { it.id != product.thumbnail?.id }
                .map { toPictureWidget(product, it) }
        )

        // Add button
        if (product.pictures.size < maxPicturesPerProduct)
            images.add(
                Container(
                    background = Theme.COLOR_PRIMARY_LIGHT,
                    borderColor = Theme.COLOR_GRAY,
                    padding = IMAGE_PADDING,
                    width = IMAGE_WIDTH,
                    height = IMAGE_HEIGHT,
                    alignment = Alignment.Center,
                    child = Button(
                        type = ButtonType.Text,
                        icon = Theme.ICON_ADD,
                        iconColor = Theme.COLOR_PRIMARY,
                        iconSize = 32.0,
                        caption = getText("page.settings.store.product.button.add-picture"),
                        action = Action(
                            type = ActionType.Prompt,
                            prompt = uploadDialog(product).toWidget()
                        ),
                    ),
                )
            )

        return ListView(
            direction = Axis.Horizontal,
            children = images,
        )
    }

    private fun toPictureWidget(product: Product, picture: PictureSummary) = Container(
        padding = IMAGE_PADDING,
        width = IMAGE_WIDTH,
        height = IMAGE_HEIGHT,
        alignment = Alignment.Center,
        borderColor = Theme.COLOR_PRIMARY_LIGHT,
        border = 1.0,
        backgroundImageUrl = picture.url,
        action = gotoUrl(
            urlBuilder.build("/settings/store/picture?product-id=${product.id}&picture-id=${picture.id}")
        )
    )

    private fun uploadDialog(product: Product) = Dialog(
        title = getText("page.settings.store.product.button.add-picture"),
        actions = listOf(
            Input(
                name = "file",
                uploadUrl = urlBuilder.build("commands/upload-picture?id=${product.id}"),
                type = InputType.Image,
                imageSource = ImageSource.Camera,
                caption = getText("page.settings.store.product.button.picture-from-camera"),
                imageMaxWidth = pictureMaxWidth,
                imageMaxHeight = pictureMaxHeight,
                action = gotoUrl(
                    url = urlBuilder.build("/settings/store/product?id=${product.id}"),
                    replacement = true
                )
            ),
            Input(
                name = "file",
                uploadUrl = urlBuilder.build("commands/upload-picture?id=${product.id}"),
                type = InputType.Image,
                imageSource = ImageSource.Gallery,
                caption = getText("page.settings.store.product.button.picture-from-gallery"),
                imageMaxWidth = pictureMaxWidth,
                imageMaxHeight = pictureMaxHeight,
                action = gotoUrl(
                    url = urlBuilder.build("/settings/store/product?id=${product.id}"),
                    replacement = true
                )
            ),
            Button(
                type = ButtonType.Text,
                caption = getText("page.settings.store.product.button.cancel"),
            ),
        )
    )

    private fun getErrorTexts(errors: Array<String>): Set<String> =
        errors.map {
            try {
                getText(it)
            } catch (ex: Exception) {
                getText("error.unexpected")
            }
        }.toSet()

    private fun getFileName(url: String): String? =
        try {
            URL(url).file
        } catch (ex: Exception) {
            null
        }

    private fun description(value: String?): String? =
        if (value == null)
            null
        else if (value.length < 160)
            value
        else
            value.substring(0, 160) + "..."
}
