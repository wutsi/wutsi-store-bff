package com.wutsi.application.store.endpoint.settings.product.add.dto

import com.wutsi.ecommerce.catalog.entity.ProductType

data class AddProductRequest(
    val title: String = "",
    val summary: String = "",
    val price: Double = 0.0,
    val subCategoryId: Long = -1,
    val quantity: Int = 0,
    val maxOrder: Int? = null,
    val type: String = ProductType.PHYSICAL.name
)
