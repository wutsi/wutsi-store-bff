package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.AddPictureRequest
import com.wutsi.platform.core.storage.StorageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URL

internal class UploadPictureCommandTest : AbstractEndpointTest() {
    @MockBean
    lateinit var storageService: StorageService

    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    private val productId = 1111L

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/upload-picture?id=$productId"
    }

    @Test
    fun upload() {
        // GIVEN
        val fileUrl = URL("http://www.wutsi.com/asset/1/toto.png")
        doReturn(fileUrl).whenever(storageService).store(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())

        // WHEN
        uploadTo(url, "toto.png")

        // THEN
        val path = argumentCaptor<String>()
        verify(storageService).store(path.capture(), any(), eq("image/png"), anyOrNull(), anyOrNull())
        kotlin.test.assertTrue(path.firstValue.startsWith("product/$productId/pictures/"))
        kotlin.test.assertTrue(path.firstValue.endsWith("toto.png"))

        val req = argumentCaptor<AddPictureRequest>()
        verify(catalogApi).addPicture(eq(productId), req.capture())
        kotlin.test.assertEquals(fileUrl.toString(), req.firstValue.url)
    }
}
