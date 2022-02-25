package com.wutsi.application.store.endpoint.order.command

import com.wutsi.ecommerce.order.entity.OrderStatus

data class FilterOrderRequest(
    val status: OrderStatus? = null
)
