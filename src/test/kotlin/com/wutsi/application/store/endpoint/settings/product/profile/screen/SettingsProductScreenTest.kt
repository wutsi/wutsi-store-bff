package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetProductResponse
import com.wutsi.ecommerce.catalog.entity.ProductType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import import

org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsProductScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store/product?id=777"
    }

    @Test
    fun profile() {
        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/settings/product/profile.json", url)
    }

    @Test
    fun profileDigital() {
        doReturn(true).whenever(togglesProvider).isDigitalProductEnabled()
        val product = createProduct(true, ProductType.NUMERIC)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/settings/product/profile-digital.json", url)
    }

    @Test
    fun noThumbnail() {
        val product = createProduct(false)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/settings/product/profile-no-thumbnail.json", url)
    }

    @Test
    fun error() {
        val product = createProduct(false)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        url =
            "http://localhost:$port/settings/store/product?id=777&errors=error.product.publish.MISSING_TITLE,error.product.publish.MISSING_PICTURE"
        assertEndpointEquals("/screens/settings/product/profile-with-errors.json", url)
    }
}
