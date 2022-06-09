package com.wutsi.application.store.endpoint.order.widget

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.qr.WutsiQrApi
import com.wutsi.platform.qr.dto.EncodeQRCodeResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class QrCodeWidgetTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @MockBean
    private lateinit var qrApi: WutsiQrApi

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/order/qr-code-widget?id=11111"
    }

    @Test
    fun order() {
        doReturn(EncodeQRCodeResponse("xxxxx")).whenever(qrApi).encode(any())

        // THEN
        assertEndpointEquals("/widgets/order/qr-code.json", url)
    }
}
