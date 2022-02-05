package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.CategorySummary
import com.wutsi.platform.catalog.dto.GetProductResponse
import com.wutsi.platform.catalog.dto.PictureSummary
import com.wutsi.platform.catalog.dto.Product
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal abstract class AbstractSettingsProductAttributeScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val product = createProduct()
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())
    }

    abstract fun attributeName(): String

    fun url(): String = "http://localhost:$port/settings/store/product/${attributeName()}?id=777"

    fun path(): String = "/screens/settings/product/${attributeName()}.json"

    @Test
    fun index() {
        assertEndpointEquals(path(), url())
    }

    protected fun createProduct() = Product(
        title = "Sample product",
        summary = "Summary of product",
        description = "This is a long description of the product",
        price = 7000.0,
        comparablePrice = 10000.0,
        visible = true,
        categories = listOf(
            CategorySummary(1, "c1"),
            CategorySummary(2, "c2")
        ),
        pictures = listOf(
            PictureSummary(
                id = 1,
                url = "https://www.imag.com/1.png"
            ),
            PictureSummary(
                id = 2,
                url = "https://www.imag.com/2.png"
            ),
            PictureSummary(
                id = 3,
                url = "https://www.imag.com/3.png"
            )
        ),
        thumbnail = PictureSummary(
            id = 3,
            url = "https://www.imag.com/3.png"
        )
    )
}
