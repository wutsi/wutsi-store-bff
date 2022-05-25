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
    fun emailDeliveryEnabled() {
        doReturn(true).whenever(togglesProvider).isShippingEmailDeliveryEnabled()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-email-delivery-enabled.json", url)
    }

    @Test
    fun internationalDeliveryEnabled() {
        doReturn(true).whenever(togglesProvider).isShippingInternationalDeliveryEnabled()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-international-delivery-enabled.json", url)
    }

    @Test
    fun localPickupEnabled() {
        doReturn(true).whenever(togglesProvider).isShippingLocalPickupEnabled()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-local-pickup-enabled.json", url)
    }

    @Test
    fun localDeliveryEnabled() {
        doReturn(true).whenever(togglesProvider).isShippingLocalDeliveryEnabled()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-local-delivery-enabled.json", url)
    }

    @Test
    fun inStorePickupEnabled() {
        doReturn(true).whenever(togglesProvider).isShippingInStorePickup()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping-in-store-pickup-enabled.json", url)
    }
}
