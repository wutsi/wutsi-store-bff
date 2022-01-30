package com.wutsi.application.store.endpoint.settings.product.picture.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.GetProductResponse
import com.wutsi.platform.catalog.dto.PictureSummary
import com.wutsi.platform.catalog.dto.Product
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsPictureScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @Test
    fun index() {
        val product = createProduct()
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        val url = "http://localhost:$port/settings/store/picture?product-id=777&picture-id=${product.thumbnail?.id}"
        assertEndpointEquals("/screens/settings/picture/picture.json", url)
    }

    private fun createProduct(withThumbnail: Boolean = true) = Product(
        id = 1,
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
        thumbnail = PictureSummary(
            id = 3,
            url = "https://www.imag.com/3.png"
        )
    )
}
