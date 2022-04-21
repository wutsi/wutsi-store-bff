package com.wutsi.application.store.endpoint.checkout.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.dto.PushTrackRequest
import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.TrackingHttpRequestInterceptor
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutSuccessScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var trackingApi: WutsiTrackingApi

    private val order = createOrder()

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        rest.interceptors.add(
            TrackingHttpRequestInterceptor(
                userAgent = "Android",
                referer = "https://www.google.com",
                ip = "10.0.2.2"
            )
        )
    }

    @Test
    fun success() {
        val url = "http://localhost:$port/checkout/success?order-id=111"
        assertEndpointEquals("/screens/checkout/success.json", url)

        val request = argumentCaptor<PushTrackRequest>()
        verify(trackingApi, times(2)).push(request.capture())

        val track1 = request.firstValue.track
        val item1 = order.items[0]
        assertEquals(ACCOUNT_ID.toString(), track1.accountId)
        assertEquals(order.merchantId.toString(), track1.merchantId)
        assertEquals(TENANT_ID, track1.tenantId)
        assertEquals(DEVICE_ID, track1.deviceId)
        assertNotNull(track1.correlationId)
        assertEquals(item1.productId.toString(), track1.productId)
        assertEquals(Page.CHECKOUT_SUCCESS, track1.page)
        assertEquals(EventType.ORDER.name, track1.event)
        assertEquals("https://www.google.com", track1.referer)
        assertEquals("Android", track1.ua)
        assertEquals("10.0.2.2", track1.ip)
        assertFalse(track1.bot)
        assertNull(track1.url)
        assertNull(track1.impressions)
        assertNull(track1.lat)
        assertNull(track1.long)
        assertNull(track1.url)
        assertEquals(item1.unitPrice * item1.quantity, track1.value)

        val track2 = request.secondValue.track
        val item2 = order.items[1]
        assertEquals(ACCOUNT_ID.toString(), track2.accountId)
        assertEquals(order.merchantId.toString(), track2.merchantId)
        assertEquals(TENANT_ID, track2.tenantId)
        assertEquals(DEVICE_ID, track2.deviceId)
        assertEquals(track1.correlationId, track2.correlationId)
        assertEquals(item2.productId.toString(), track2.productId)
        assertEquals(Page.CHECKOUT_SUCCESS, track2.page)
        assertEquals(EventType.ORDER.name, track2.event)
        assertEquals("https://www.google.com", track2.referer)
        assertEquals("Android", track2.ua)
        assertEquals("10.0.2.2", track2.ip)
        assertFalse(track2.bot)
        assertNull(track2.url)
        assertNull(track2.impressions)
        assertNull(track2.lat)
        assertNull(track2.long)
        assertNull(track2.url)
        assertEquals(item2.unitPrice * item2.quantity, track2.value)
    }

    @Test
    fun error() {
        val url = "http://localhost:$port/checkout/success?order-id=111&error=failure"
        assertEndpointEquals("/screens/checkout/error.json", url)

        verify(trackingApi, never()).push(any())
    }
}
