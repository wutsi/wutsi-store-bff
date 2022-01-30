package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.catalog.dto.AddPictureRequest
import com.wutsi.platform.core.storage.StorageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URL

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UploadPictureCommandTest : AbstractEndpointTest() {
    @MockBean
    lateinit var storageService: StorageService

    @LocalServerPort
    public val port: Int = 0

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

    private fun uploadTo(url: String, filename: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // This nested HttpEntiy is important to create the correct
        // Content-Disposition entry with metadata "name" and "filename"
        val fileMap = LinkedMultiValueMap<String, String>()
        val contentDisposition = ContentDisposition
            .builder("form-data")
            .name("file")
            .filename(filename)
            .build()
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        val fileEntity = HttpEntity<ByteArray>("test".toByteArray(), fileMap)

        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", fileEntity)

        val requestEntity = HttpEntity<MultiValueMap<String, Any>>(body, headers)
        rest.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            Any::class.java
        )
    }
}
