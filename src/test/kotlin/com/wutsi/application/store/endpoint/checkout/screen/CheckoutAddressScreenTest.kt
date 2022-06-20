package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.entity.CityEntity
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.ListAddressResponse
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.AddressType
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

    private val order = Order(
        id = "111",
        addressType = AddressType.POSTAL.name
    )

    @Test
    fun postal() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val addresses = listOf(
            createAddress(id = 1, firstName = "Roger"),
            createAddress(id = 2, firstName = "Milla")
        )
        doReturn(ListAddressResponse(addresses)).whenever(orderApi).listAddresses(order.addressType)

        val city = CityEntity(id = 111, name = "Yaounde", country = "CM")
        doReturn(city).whenever(cityService).get(any())

        // WHEN
        val url = "http://localhost:$port/checkout/address?order-id=111"
        assertEndpointEquals("/screens/checkout/addresses-postal.json", url)
    }

    @Test
    fun email() {
        // GIVEN
        val order = Order(
            id = "111",
            addressType = AddressType.EMAIL.name
        )
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val addresses = listOf(
            createAddress(id = 1, firstName = "Roger", email = "roger.milla@gmail.com", type = AddressType.EMAIL),
        )
        doReturn(ListAddressResponse(addresses)).whenever(orderApi).listAddresses(order.addressType)

        // WHEN
        val url = "http://localhost:$port/checkout/address?order-id=111"
        assertEndpointEquals("/screens/checkout/addresses-email.json", url)
    }

    @Test
    fun empty() {
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())
        doReturn(ListAddressResponse()).whenever(orderApi).listAddresses(anyOrNull())

        val url = "http://localhost:$port/checkout/address?order-id=111"
        assertEndpointEquals("/screens/checkout/addresses-empty.json", url)
    }
}
