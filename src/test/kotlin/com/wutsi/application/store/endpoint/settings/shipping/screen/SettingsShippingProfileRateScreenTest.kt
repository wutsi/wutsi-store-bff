package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.GetShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsShippingProfileRateScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    @Test
    fun index() {
        val shipping = createShipping(ShippingType.LOCAL_PICKUP)
        doReturn(GetShippingResponse(shipping)).whenever(shippingApi).getShipping(any())

        val url = "http://localhost:$port/settings/store/shipping/attribute/rate?id=111"
        assertEndpointEquals("/screens/settings/shipping/rate.json", url)
    }
}
