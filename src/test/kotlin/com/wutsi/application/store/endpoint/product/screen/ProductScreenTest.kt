package com.wutsi.application.store.endpoint.product.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.dto.PushTrackRequest
import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.TrackingHttpRequestInterceptor
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.cart.dto.GetCartResponse
import com.wutsi.ecommerce.cart.dto.Product
import com.wutsi.ecommerce.catalog.dto.GetProductResponse
import com.wutsi.ecommerce.catalog.dto.SearchProductResponse
import com.wutsi.ecommerce.catalog.entity.ProductType
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.SearchRateResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ProductScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    @MockBean
    private lateinit var trackingApi: WutsiTrackingApi

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/product?id=11"

        rest.interceptors.add(
            TrackingHttpRequestInterceptor(
                userAgent = "Android",
                referer = "https://www.google.com",
                ip = "10.0.2.2"
            )
        )

        val rates = listOf(
            createRateSummary(ShippingType.LOCAL_PICKUP),
            createRateSummary(ShippingType.LOCAL_DELIVERY, rate = 1000.0),
            createRateSummary(ShippingType.INTERNATIONAL_SHIPPING, rate = 2000.0)
        )
        doReturn(SearchRateResponse(rates)).whenever(shippingApi).searchRate(any())

        val products = listOf(
            createProductSummary(id = 1, accountId = 77, categoryId = 1, subCategoryId = 5),
            createProductSummary(id = 2, accountId = 77, subCategoryId = 2),
            createProductSummary(id = 3, accountId = 77, subCategoryId = 3),
            createProductSummary(id = 4),
            createProductSummary(id = 5),
            createProductSummary(id = 6),
            createProductSummary(id = 7),
            createProductSummary(id = 8),
            createProductSummary(id = 9),
            createProductSummary(id = 10),
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
    }

    @Test
    fun productWithImage() {
        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-with-image.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun numericProduct() {
        val product = createProduct(true, type = ProductType.NUMERIC)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-numeric.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun productWithCartEnabled() {
        doReturn(true).whenever(togglesProvider).isCartEnabled()

        val cart = Cart(
            products = listOf(
                Product(productId = 11L, quantity = 1),
                Product(productId = 20L, quantity = 3)
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-with-cart-enabled.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun productWithProductInCart() {
        doReturn(true).whenever(togglesProvider).isCartEnabled()

        val product = createProduct(true)
        val cart = Cart(
            products = listOf(
                Product(productId = product.id, quantity = 5),
                Product(productId = 20L, quantity = 3)
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-in-cart.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun productNoStock() {
        doReturn(SearchRateResponse()).whenever(shippingApi).searchRate(any())

        val product = createProduct(withThumbnail = true, quantity = 0)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-no-stock.json", url)
        assertTrackPushed(product)
    }

    private fun assertTrackPushed(product: com.wutsi.ecommerce.catalog.dto.Product) {
        val request = argumentCaptor<PushTrackRequest>()
        verify(trackingApi).push(request.capture())

        val track = request.firstValue.track
        assertEquals(ACCOUNT_ID.toString(), track.accountId)
        assertEquals(product.accountId.toString(), track.merchantId)
        assertEquals(TENANT_ID, track.tenantId)
        assertEquals(DEVICE_ID, track.deviceId)
        assertNotNull(track.correlationId)
        assertEquals(product.id.toString(), track.productId)
        assertEquals(Page.PRODUCT, track.page)
        assertEquals(EventType.VIEW.name, track.event)
        assertEquals("https://www.google.com", track.referer)
        assertEquals("Android", track.ua)
        assertEquals("10.0.2.2", track.ip)
        assertFalse(track.bot)
        assertNull(track.url)
        assertNull(track.impressions)
        assertNull(track.lat)
        assertNull(track.long)
        assertNull(track.url)
        assertNull(track.value)
    }
}
