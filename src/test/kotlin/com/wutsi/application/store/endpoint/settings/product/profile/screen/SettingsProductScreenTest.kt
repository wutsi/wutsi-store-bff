package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.GetProductResponse
import com.wutsi.platform.catalog.dto.PictureSummary
import com.wutsi.platform.catalog.dto.Product
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/product?id=777"
    }

    @Test
    fun withThumbnail() {
        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/settings/product/profile.json", url)
    }

    @Test
    fun withoutThumbnail() {
        val product = createProduct(false)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/settings/product/profile-no-thumbnail.json", url)
    }

    private fun createProduct(withThumbnail: Boolean = true) = Product(
        title = "Sample product",
        summary = "Summary of product",
        description = "This is a long description of the product",
        price = 7000.0,
        comparablePrice = 10000.0,
        visible = true,
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
        thumbnail = if (withThumbnail)
            PictureSummary(
                id = 3,
                url = "https://www.imag.com/3.png"
            )
        else
            null
    )
}
