package com.wutsi.application.store.endpoint.order.dto

data class ChangeOrderStatusRequest(
    val reason: String? = null,
    val comment: String? = null
)
