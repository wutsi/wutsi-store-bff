package com.wutsi.application.store.endpoint.checkout.dto

data class SaveShippingAddressRequest(
    val firstName: String = "",
    val lastName: String = "",
    val street: String? = null,
    val email: String? = null,
    val cityId: Long? = null
)
