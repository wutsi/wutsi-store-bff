package com.wutsi.application.store.endpoint.checkout.dto

data class AuthorizeOrderPaymentRequest(
    val paymentToken: String = "",
)
