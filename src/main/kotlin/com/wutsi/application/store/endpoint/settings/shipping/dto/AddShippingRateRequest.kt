package com.wutsi.application.store.endpoint.settings.shipping.dto

data class AddShippingRateRequest(
    val amount: Double = 0.0,
    val cityId: Long? = null,
    val country: String = "",
)
