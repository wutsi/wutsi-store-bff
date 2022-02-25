package com.wutsi.application.store.endpoint.order.dto

import com.wutsi.ecommerce.order.entity.OrderStatus

data class FilterOrderRequest(
    val status: OrderStatus? = null
)
