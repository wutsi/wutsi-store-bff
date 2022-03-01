package com.wutsi.application.store.endpoint.settings.shipping.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.ListShippingResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
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

    @Test
    fun index() {
        val shippings = listOf(
            createShippingSummary(ShippingType.INTERNATIONAL_SHIPPING),
            createShippingSummary(ShippingType.EMAIL_DELIVERY)
        )
        doReturn(ListShippingResponse(shippings)).whenever(shippingApi).listShipping()

        val url = "http://localhost:$port/settings/store/shipping"
        assertEndpointEquals("/screens/settings/shipping/shipping.json", url)
    }
}
