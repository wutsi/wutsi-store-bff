package com.wutsi.application.store.endpoint.checkout.widget

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.GetTransactionResponse
import com.wutsi.platform.payment.dto.Transaction
import com.wutsi.platform.payment.error.ErrorURN
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CheckoutStatusWidgetTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    @MockBean
    private lateinit var paymentApi: WutsiPaymentApi

    @Test
    fun pending() {
        val tx = createTransaction(status = Status.PENDING)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        val url = "http://localhost:$port/widgets/checkout-status?transaction-id=1111&count=1"
        assertEndpointEquals("/widgets/checkout/status-pending.json", url)

        verify(cartApi, never()).emptyCart(any())
    }

    @Test
    fun success() {
        val tx = createTransaction(status = Status.SUCCESSFUL)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        val url = "http://localhost:$port/widgets/checkout-status?transaction-id=1111&count=1"
        assertEndpointEquals("/widgets/checkout/status-success.json", url)

        verify(cartApi).emptyCart(tx.recipientId!!)
    }

    @Test
    fun failed() {
        val ex = createFeignException(
            errorCode = ErrorURN.TRANSACTION_FAILED.urn
        )
        doThrow(ex).whenever(paymentApi).getTransaction(any())

        val url = "http://localhost:$port/widgets/checkout-status?transaction-id=1111&count=1"
        assertEndpointEquals("/widgets/checkout/status-failed.json", url)

        verify(cartApi, never()).emptyCart(any())
    }

    @Test
    fun tooManyRetries() {
        val tx = createTransaction(status = Status.PENDING)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        val url =
            "http://localhost:$port/widgets/checkout-status?transaction-id=1111&count=" + (CheckoutStatusWidget.MAX_COUNT + 1)
        assertEndpointEquals("/widgets/checkout/status-too-many-tries.json", url)
    }

    private fun createTransaction(status: Status) = Transaction(
        status = status.name,
        orderId = "12090239",
        recipientId = 111
    )
}
