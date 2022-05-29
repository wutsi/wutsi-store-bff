package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me/order")
class MyOrderScreen(
    orderApi: WutsiOrderApi,
    accountApi: WutsiAccountApi,
    catalogApi: WutsiCatalogApi,
    shippingApi: WutsiShippingApi,
    tenantProvider: TenantProvider,
) : AbstractOrderScreen(orderApi, accountApi, catalogApi, shippingApi, tenantProvider) {
    override fun getPageId() = Page.MY_ORDER
    override fun showMerchantInfo() = true
    override fun getAppBarAction(order: Order, shipping: Shipping?): WidgetAware? = null
}
