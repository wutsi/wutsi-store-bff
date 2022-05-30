package com.wutsi.application.store.endpoint

import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.CartIcon
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import java.net.URL

abstract class AbstractQuery : AbstractEndpoint() {
    protected fun getFileName(url: String?): String? {
        url ?: return null

        try {
            val file = URL(url).file
            val i = file.lastIndexOf("/")
            return if (i >= 0)
                file.substring(i + 1)
            else
                file
        } catch (ex: Exception) {
            return null
        }
    }

    protected fun titleBarActions(
        productId: Long?,
        merchantId: Long,
        shareUrl: String?,
        whatsappUrl: String?,
        cart: Cart?
    ): List<WidgetAware> {
        return listOfNotNull(
            whatsappUrl?.let {
                Container(
                    padding = 4.0,
                    child = CircleAvatar(
                        radius = 20.0,
                        backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                        child = IconButton(
                            icon = Theme.ICON_CHAT,
                            size = 20.0,
                            action = Action(
                                type = ActionType.Navigate,
                                url = it,
                                trackEvent = productId?.let { EventType.CHAT.name },
                                trackProductId = productId?.toString()
                            )
                        )
                    ),
                )
            },
            shareUrl?.let {
                Container(
                    padding = 4.0,
                    child = CircleAvatar(
                        radius = 20.0,
                        backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                        child = IconButton(
                            icon = Theme.ICON_SHARE,
                            size = 20.0,
                            action = Action(
                                type = ActionType.Share,
                                url = it,
                                trackEvent = productId?.let { EventType.SHARE.name },
                                trackProductId = productId?.toString()
                            )
                        ),
                    )
                )
            },
            cart?.let {
                Container(
                    padding = 4.0,
                    child = CircleAvatar(
                        radius = 20.0,
                        backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                        child = CartIcon(
                            productCount = it.products.size,
                            size = 20.0,
                            action = gotoUrl(urlBuilder.build("cart?merchant-id=$merchantId"))
                        ),
                    )
                )
            }
        )
    }

    protected fun toSectionWidget(
        child: WidgetAware,
        padding: Double? = 10.0,
        background: String? = Theme.COLOR_WHITE
    ) = Container(
        padding = padding,
        margin = 5.0,
        border = 1.0,
        borderColor = Theme.COLOR_GRAY_LIGHT,
        background = background,
        width = Double.MAX_VALUE,
        child = child,
    )
}
