package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.ListShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsShippingScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val shippings = ShippingType.values().map { createShippingSummary(it) }
        doReturn(ListShippingResponse(shippings)).whenever(shippingApi).listShipping()
    }

    @Test
    fun index() {
        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping.json", url)
    }

    @Test
    fun digitalProductEnabled() {
        doReturn(true).whenever(togglesProvider).isDigitalProductEnabled()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-digital-product-enabled.json", url)
    }

    @Test
    fun internationalShippingEnabled() {
        doReturn(true).whenever(togglesProvider).isInternationalShippingEnabled()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-international-enabled.json", url)
    }
}
