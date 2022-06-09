package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.entity.CityEntity
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ListAddressResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutAddressScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var cityService: CityService

    @Test
    fun index() {
        val addresses = listOf(
            createAddress(id = 1, firstName = "Roger"),
            createAddress(id = 2, firstName = "Milla")
        )
        doReturn(ListAddressResponse(addresses)).whenever(orderApi).listAddresses()

        val city = CityEntity(id = 111, name = "Yaounde", country = "CM")
        doReturn(city).whenever(cityService).get(any())

        val url = "http://localhost:$port/checkout/address?order-id=111"
        assertEndpointEquals("/screens/checkout/addresses.json", url)
    }

    @Test
    fun empty() {
        doReturn(ListAddressResponse()).whenever(orderApi).listAddresses()

        val url = "http://localhost:$port/checkout/address?order-id=111"
        assertEndpointEquals("/screens/checkout/addresses-empty.json", url)
    }
}
