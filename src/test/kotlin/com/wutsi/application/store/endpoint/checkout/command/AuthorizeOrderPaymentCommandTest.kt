package com.wutsi.application.store.endpoint.checkout.command

import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.checkout.dto.AuthorizeOrderPaymentRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class AuthorizeOrderPaymentCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String
    private val orderId = "123"
    private val idempotencyKey = "567"

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url =
            "http://localhost:$port/commands/authorize-order-payment?order-id=$orderId&idempotency-key=$idempotencyKey"
    }

    @Test
    fun create() {
        // WHEN
        val request = AuthorizeOrderPaymentRequest(
            paymentToken = "xxx"
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals(
            "https://wutsi-gateway-test.herokuapp.com/login/?phone=%2B12376666666677777&icon=f197&screen-id=page.checkout.pin&title=Authorization&sub-title=Enter+your+PIN+to+authorize+the+payment&auth=false&return-to-route=false&return-url=http%3A%2F%2Flocalhost%3A0%2Fcommands%2Fpay-order%3Forder-id%3D123%26payment-token%3Dxxx%26idempotency-key%3D567",
            action.url
        )
    }
}
