package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.order.dto.FilterOrderRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.dto.SearchOrderRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me/orders")
class MyOrdersScreen(
    accountApi: WutsiAccountApi,
    tenantProvider: TenantProvider,
    private val orderApi: WutsiOrderApi,
) : AbstractOrderListScreen(accountApi, tenantProvider) {
    override fun getPageId() = Page.MY_ORDERS

    override fun getTitle() = getText("page.my-orders.app-bar.title")

    override fun getOrders(request: FilterOrderRequest?) = orderApi.searchOrders(
        request = SearchOrderRequest(
            accountId = securityContext.currentAccountId(),
            status = OrderStatus.values().filter { it != OrderStatus.CREATED && it != OrderStatus.CANCELLED }
                .map { it.name },
            limit = 30,
        )
    ).orders

    override fun getAction(order: OrderSummary) = gotoUrl(
        url = urlBuilder.build("/me/order?id=${order.id}")
    )

    override fun getAccountId(order: OrderSummary) = order.merchantId
}