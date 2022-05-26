package com.wutsi.application.store.service

import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.Product
import com.wutsi.ecommerce.shipping.dto.RateSummary
import com.wutsi.ecommerce.shipping.dto.SearchRateRequest
import com.wutsi.platform.account.dto.Account
import org.springframework.stereotype.Service

@Service
class ShippingService(
    private val shippingApi: WutsiShippingApi,
    private val catalogApi: WutsiCatalogApi,
) {
    fun findShippingRates(account: Account, order: Order): List<RateSummary> {
        val products = catalogApi.searchProducts(
            SearchProductRequest(
                productIds = order.items.map { it.productId },
                limit = order.items.size
            )
        ).products
        return shippingApi.searchRate(
            SearchRateRequest(
                country = account.country,
                cityId = account.cityId,
                accountId = order.merchantId,
                products = products.map {
                    Product(
                        productId = it.id,
                        productType = it.type,
                        quantity = it.quantity
                    )
                }
            )
        ).rates
    }

    fun findShippingRate(shippingId: Long, account: Account, order: Order): RateSummary? =
        findShippingRates(account, order).find { it.shippingId == shippingId }
}
