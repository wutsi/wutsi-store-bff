package com.wutsi.application.store.endpoint.settings.product.add.dto

data class AddProductRequest(
    val title: String = "",
    val summary: String = "",
    val price: Double = 0.0,
    val subCategoryId: Long = -1,
)
