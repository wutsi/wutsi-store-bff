package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.shipping.WutsiShippingApi
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

    override fun getAppBarAction(order: Order): WidgetAware? {
        val buttons = getAppBarButtons(order)
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

    private fun getAppBarButtons(order: Order): List<WidgetAware> =
        if (order.status == OrderStatus.OPENED.name)
            listOf(
                Button(
                    caption = getText("page.order.button.close"),
                    action = gotoUrl(urlBuilder.build("/order/close?id=${order.id}"))
                ),
                Button(
                    type = ButtonType.Outlined,
                    caption = getText("page.order.button.cancel"),
                    action = gotoUrl(urlBuilder.build("/order/cancel?id=${order.id}"))
                )
            )
        else
            emptyList()
}
