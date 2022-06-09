package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.UpdateProductAttributeRequest
import com.wutsi.platform.core.storage.StorageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import import

org.springframework.boot.test.web.server.LocalServerPort
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UploadNumericFileCommandTest : AbstractEndpointTest() {
    @MockBean
    lateinit var storageService: StorageService

    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    private val productId = 1111L

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/upload-numeric-file?id=$productId"
    }

    @Test
    fun upload() {
        // GIVEN
        val fileUrl = URL("http://www.wutsi.com/asset/1/toto.zip")
        doReturn(fileUrl).whenever(storageService).store(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())

        // WHEN
        uploadTo(url, "file.zip")

        // THEN
        val path = argumentCaptor<String>()
        verify(storageService).store(path.capture(), any(), eq("application/zip"), anyOrNull(), anyOrNull())
        assertTrue(path.firstValue.startsWith("product/$productId/files/"))
        assertTrue(path.firstValue.endsWith("file.zip"))

        val req = argumentCaptor<UpdateProductAttributeRequest>()
        verify(catalogApi).updateProductAttribute(eq(productId), eq("numeric-file-url"), req.capture())
        assertEquals(fileUrl.toString(), req.firstValue.value)
    }
}
