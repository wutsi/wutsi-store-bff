package com.wutsi.application.store.endpoint

import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.ui.CartIcon
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.Transaction
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

    protected fun toTransactionStatusWidget(tx: Transaction?, error: String? = null): WidgetAware {
        return if (error != null)
            toTransactionStatusWidget(
                Icon(code = Theme.ICON_ERROR, size = 40.0, color = Theme.COLOR_DANGER),
                Text(
                    error,
                    color = Theme.COLOR_DANGER,
                    alignment = TextAlignment.Center
                )
            )
        else if (tx?.status == Status.SUCCESSFUL.name)
            toTransactionStatusWidget(
                Icon(code = Theme.ICON_CHECK, size = 40.0, color = Theme.COLOR_SUCCESS),
                Text(
                    getText("widget.checkout.processing.success"),
                    color = Theme.COLOR_SUCCESS,
                    alignment = TextAlignment.Center
                )
            )
        else if (tx?.status == Status.FAILED.name)
            toTransactionStatusWidget(
                Icon(code = Theme.ICON_ERROR, size = 40.0, color = Theme.COLOR_DANGER),
                Text(
                    getTransactionErrorText(tx.errorCode),
                    color = Theme.COLOR_DANGER,
                    alignment = TextAlignment.Center
                )
            )
        else
            toTransactionStatusWidget(
                Icon(code = Theme.ICON_PENDING, size = 40.0, color = Theme.COLOR_PRIMARY),
                Text(
                    getText("widget.checkout.processing.pending"),
                    alignment = TextAlignment.Center
                )
            )
    }

    private fun toTransactionStatusWidget(icon: Icon, text: Text): WidgetAware {
        return Column(
            children = listOf(
                icon,
                Container(
                    padding = 10.0,
                    alignment = Alignment.Center,
                    child = text
                ),
                Container(
                    padding = 10.0,
                    child = Button(
                        caption = getText("widget.checkout.processing.button.OK"),
                        action = gotoHomeScreen()
                    )
                )
            )
        )
    }
}
