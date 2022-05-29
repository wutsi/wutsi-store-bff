package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CircleAvatar
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Dialog
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.DialogType
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order")
class OrderScreen(
    orderApi: WutsiOrderApi,
    accountApi: WutsiAccountApi,
    catalogApi: WutsiCatalogApi,
    shippingApi: WutsiShippingApi,
    tenantProvider: TenantProvider,
) : AbstractOrderScreen(orderApi, accountApi, catalogApi, shippingApi, tenantProvider) {
    override fun getPageId() = Page.ORDER
    override fun showMerchantInfo() = false

    override fun getAppBarAction(order: Order, shipping: Shipping?): WidgetAware? {
        val buttons = getAppBarButtons(order, shipping)
        if (buttons.isEmpty())
            return null

        return Container(
            padding = 4.0,
            child = CircleAvatar(
                radius = 20.0,
                backgroundColor = Theme.COLOR_PRIMARY_LIGHT,
                child = IconButton(
                    icon = Theme.ICON_MORE,
                    size = 20.0,
                    action = Action(
                        type = ActionType.Prompt,
                        prompt = Dialog(
                            type = DialogType.Confirm,
                            actions = buttons
                        ).toWidget()
                    )
                )
            )
        )
    }

    private fun getAppBarButtons(order: Order, shipping: Shipping?): List<WidgetAware> {
        val children = mutableListOf<WidgetAware>()
        if (order.status == OrderStatus.OPENED.name)
            children.add(
                Button(
                    caption = getText("page.order.button.close"),
                    action = gotoUrl(urlBuilder.build("/order/close?id=${order.id}"))
                ),
            )
        else if (isAvailableForInStorePickup(order, shipping))
            children.add(
                Button(
                    caption = getText("page.order.button.pickup"),
                    action = gotoUrl(urlBuilder.build("/order/pickup?id=${order.id}"))
                )
            )

        if (order.status != OrderStatus.EXPIRED.name && order.status != OrderStatus.CANCELLED.name && order.status != OrderStatus.DELIVERED.name)
            children.add(
                Button(
                    type = ButtonType.Outlined,
                    caption = getText("page.order.button.cancel"),
                    action = gotoUrl(urlBuilder.build("/order/cancel?id=${order.id}"))
                )
            )

        return children
    }

    private fun isAvailableForInStorePickup(order: Order, shipping: Shipping?): Boolean =
        togglesProvider.isShippingInStorePickup() &&
            shipping != null &&
            shipping.type == ShippingType.IN_STORE_PICKUP.name &&
            order.status == OrderStatus.READY_FOR_PICKUP.name
}
