package com.wutsi.application.store.endpoint.settings.product.add.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.settings.product.add.dto.AddProductRequest
import com.wutsi.ecommerce.catalog.dto.CreateProductRequest
import com.wutsi.ecommerce.catalog.dto.CreateProductResponse
import com.wutsi.ecommerce.catalog.entity.ProductType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class AddProductCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/add-product?category-id=111"
    }

    @Test
    fun index() {
        // GIVEN
        val productId = 777L
        doReturn(CreateProductResponse(productId)).whenever(catalogApi).createProduct(any())

        // WHEN
        val request = AddProductRequest(
            title = "Product1",
            summary = "This is a nice summary",
            price = 10000.0,
            subCategoryId = 333L,
            type = ProductType.NUMERIC.name,
            quantity = 100,
            maxOrder = 5,
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<CreateProductRequest>()
        verify(catalogApi).createProduct(req.capture())
        assertEquals(request.title, req.firstValue.title)
        assertEquals(request.summary, req.firstValue.summary)
        assertEquals(request.price, req.firstValue.price)
        assertEquals(request.subCategoryId, req.firstValue.subCategoryId)
        assertEquals(111L, req.firstValue.categoryId)
        assertNull(req.firstValue.comparablePrice)
        assertNull(req.firstValue.description)
        assertEquals(request.type, req.firstValue.type)
        assertEquals(request.quantity, req.firstValue.quantity)
        assertEquals(request.maxOrder, req.firstValue.maxOrder)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/settings/store/product?id=$productId", action.url)
    }
}
