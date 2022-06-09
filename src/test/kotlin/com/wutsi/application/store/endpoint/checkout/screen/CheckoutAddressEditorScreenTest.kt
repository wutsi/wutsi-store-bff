package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.entity.CityEntity
import com.wutsi.application.shared.service.CityService
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import import

org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutAddressEditorScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var cityService: CityService

    @Test
    fun index() {
        val cities = listOf(
            CityEntity(id = 1, name = "Yaounde", country = "CM"),
            CityEntity(id = 2, name = "Douala", country = "CM")
        )
        doReturn(cities).whenever(cityService).search(anyOrNull(), any())

        val url = "http://localhost:$port/checkout/address-editor?order-id=111"
        val request = mapOf("country" to "CM")
        assertEndpointEquals("/screens/checkout/address-editor.json", url, request)
    }
}
