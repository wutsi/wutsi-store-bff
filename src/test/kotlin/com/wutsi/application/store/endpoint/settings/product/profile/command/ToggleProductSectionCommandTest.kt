package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.UpdateProductAttributeRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import kotlin.test.assertEquals

internal class ToggleProductSectionCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private val productId = 777L

    private val sectionId = 55L

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/toggle-product-section?id=$productId&section-id=$sectionId"
    }

    @Test
    fun addToSection() {
        // WHEN
        val request = UpdateProductAttributeRequest(
            value = "true"
        )
        val response = rest.postForEntity(url, request, Any::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).addToSection(eq(sectionId), eq(productId))
    }

    @Test
    fun removeFromSection() {
        // WHEN
        val request = UpdateProductAttributeRequest(
            value = "false"
        )
        val response = rest.postForEntity(url, request, Any::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).removeFromSection(eq(sectionId), eq(productId))
    }
}
