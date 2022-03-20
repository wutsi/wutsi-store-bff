package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.entity.CityEntity
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.GetShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsShippingProfileScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @MockBean
    private lateinit var cityService: CityService

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val city = CityEntity(id = 11, name = "Yaounde", country = "CM")
        doReturn(city).whenever(cityService).get(any())
    }

    @Test
    fun localPickup() {
        val shipping = createShipping(ShippingType.LOCAL_PICKUP)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        val url = "http://localhost:$port/settings/store/shipping/profile?id=111"
        assertEndpointEquals("/screens/settings/shipping/profile-local-pickup.json", url)
    }

    @Test
    fun localDelivery() {
        val shipping = createShipping(ShippingType.LOCAL_DELIVERY)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        val url = "http://localhost:$port/settings/store/shipping/profile?id=111"
        assertEndpointEquals("/screens/settings/shipping/profile-local-delivery.json", url)
    }

    @Test
    fun emailDelivery() {
        val shipping = createShipping(ShippingType.EMAIL_DELIVERY)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        val url = "http://localhost:$port/settings/store/shipping/profile?id=111"
        assertEndpointEquals("/screens/settings/shipping/profile-email-delivery.json", url)
    }
}
