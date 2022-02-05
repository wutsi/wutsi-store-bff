package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.catalog.dto.AddCategoryRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class AddProductCategoryCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private val productId = 777L
    private val categoryId = 555L

    @Test
    fun add() {
        // WHEN
        val url = "http://localhost:$port/commands/add-product-category?product-id=$productId&category-id=$categoryId"
        val request = com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = "true"
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<AddCategoryRequest>()
        verify(catalogApi).addCategory(eq(productId), req.capture())
        assertEquals(categoryId, req.firstValue.categoryId)
    }

    @Test
    fun remove() {
        // WHEN
        val url = "http://localhost:$port/commands/add-product-category?product-id=$productId&category-id=$categoryId"
        val request = com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = "false"
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<AddCategoryRequest>()
        verify(catalogApi).removeCategory(productId, categoryId)
    }
}
