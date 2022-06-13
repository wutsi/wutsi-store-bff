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
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsShippingProfileCityScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var cityService: CityService

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @Test
    fun index() {
        // GIVEN
        val city = CityEntity(id = 1111, name = "Yaounde", country = "CM")
        doReturn(city).whenever(cityService).get(any())

        val cities = listOf(
            CityEntity(id = 1111, name = "Yaounde", country = "CM"),
            CityEntity(id = 1111, name = "Douala", country = "CM"),
            CityEntity(id = 1111, name = "Baffoussam", country = "CM")
        )
        doReturn(cities).whenever(cityService).search(any(), any())

        val shipping = createShipping(ShippingType.LOCAL_PICKUP, cityId = city.id)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        // WHEN
        val url = "http://localhost:$port/settings/store/shipping/attribute/city-id?id=111"
        assertEndpointEquals("/screens/settings/shipping/city-id.json", url)
    }
}
