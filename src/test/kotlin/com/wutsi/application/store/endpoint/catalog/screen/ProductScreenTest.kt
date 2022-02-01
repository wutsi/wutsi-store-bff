package com.wutsi.application.store.endpoint.catalog.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.GetProductResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ProductScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/product?id=11"
    }

    @Test
    fun productWithImage() {
        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/catalog/product-with-image.json", url)
    }

    @Test
    fun productWithoutImage() {
        val product = createProduct(false)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/catalog/product-without-image.json", url)
    }
}
