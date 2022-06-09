package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.error.ErrorURN
import com.wutsi.ecommerce.catalog.error.PublishError
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import import

org.springframework.boot.test.web.server.LocalServerPort

internal class PublishProductCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private val productId = 777L

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/publish-product?id=$productId"
    }

    @Test
    fun publish() {
        // WHEN
        val response = rest.postForEntity(url, createPayload(true), Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).publishProduct(productId)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/settings/store/product?id=$productId", action.url)
        assertEquals(true, action.replacement)
    }

    @Test
    fun error() {
        // GIVEN
        val ex = createFeignException(
            errorCode = ErrorURN.PUBLISH_ERROR.urn,
            data = mapOf(
                "publishing-errors" to listOf(
                    PublishError.MISSING_TITLE,
                    PublishError.MISSING_NUMERIC_FILE
                )
            )
        )
        doThrow(ex).whenever(catalogApi).publishProduct(any())

        // WHEN
        val response = rest.postForEntity(url, createPayload(true), Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).publishProduct(productId)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals(
            "http://localhost:0/settings/store/product?id=777&errors=error.product.publish.MISSING_TITLE,error.product.publish.MISSING_NUMERIC_FILE",
            action.url
        )
        assertEquals(true, action.replacement)
    }

    @Test
    fun unpublish() {
        // WHEN
        val response = rest.postForEntity(url, createPayload(false), Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).unpublishProduct(productId)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/settings/store/product?id=$productId", action.url)
        assertEquals(true, action.replacement)
    }

    private fun createPayload(value: Boolean) =
        com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = value.toString()
        )
}
