package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.cart.dto.GetCartResponse
import com.wutsi.ecommerce.cart.dto.Product
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.CreateOrderRequest
import com.wutsi.ecommerce.order.dto.CreateOrderResponse
import com.wutsi.ecommerce.order.entity.AddressType
import com.wutsi.ecommerce.order.error.ErrorURN
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    private lateinit var url: String

    private val cart = Cart(
        merchantId = 111,
        products = listOf(
            Product(productId = 1, quantity = 3),
            Product(productId = 3, quantity = 1)
        )
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/create-order?merchant-id=111"
    }

    @Test
    fun create() {
        // GIVEN
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        doReturn(CreateOrderResponse("555")).whenever(orderApi).createOrder(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val request = argumentCaptor<CreateOrderRequest>()
        verify(orderApi).createOrder(request.capture())
        assertEquals(111L, request.firstValue.merchantId)
        assertEquals(AddressType.POSTAL.name, request.firstValue.addressType)
        assertEquals(2, request.firstValue.items.size)
        assertEquals(cart.products[0].productId, request.firstValue.items[0].productId)
        assertEquals(cart.products[0].quantity, request.firstValue.items[0].quantity)
        assertEquals(cart.products[1].productId, request.firstValue.items[1].productId)
        assertEquals(cart.products[1].quantity, request.firstValue.items[1].quantity)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/review?order-id=555", action.url)
    }

    @Test
    fun createShippingEnabled() {
        // GIVEN
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())
        doReturn(true).whenever(togglesProvider).isShippingEnabled()

        doReturn(CreateOrderResponse("555")).whenever(orderApi).createOrder(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val request = argumentCaptor<CreateOrderRequest>()
        verify(orderApi).createOrder(request.capture())
        assertEquals(111L, request.firstValue.merchantId)
        assertEquals(2, request.firstValue.items.size)
        assertEquals(cart.products[0].productId, request.firstValue.items[0].productId)
        assertEquals(cart.products[0].quantity, request.firstValue.items[0].quantity)
        assertEquals(cart.products[1].productId, request.firstValue.items[1].productId)
        assertEquals(cart.products[1].quantity, request.firstValue.items[1].quantity)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/address?order-id=555", action.url)
    }

    @Test
    fun availabilityError() {
        // GIVEN
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val ex = createFeignException(ErrorURN.PRODUCT_AVAILABILITY_ERROR.urn)
        doThrow(ex).whenever(orderApi).createOrder(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(getText("error.order.PRODUCT_AVAILABILITY_ERROR"), action.prompt?.attributes?.get("message"))
    }

    @Test
    fun unexpectedError() {
        // GIVEN
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val ex = createFeignException("xxx")
        doThrow(ex).whenever(orderApi).createOrder(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(getText("error.unexpected"), action.prompt?.attributes?.get("message"))
    }
}
