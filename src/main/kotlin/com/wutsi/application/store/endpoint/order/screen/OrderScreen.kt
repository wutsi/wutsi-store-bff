package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.service.CityService
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.shipping.WutsiShippingApi
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
    cityService: CityService
) : AbstractOrderScreen(orderApi, accountApi, catalogApi, shippingApi, tenantProvider, cityService) {
    override fun getPageId() = Page.ORDER
    override fun showMerchantInfo() = false
}
