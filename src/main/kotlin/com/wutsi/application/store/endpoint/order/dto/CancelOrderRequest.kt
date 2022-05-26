package com.wutsi.application.store.endpoint.order.dto

data class CancelOrderRequest(
    val reason: String? = null,
    val comment: String? = null
)
