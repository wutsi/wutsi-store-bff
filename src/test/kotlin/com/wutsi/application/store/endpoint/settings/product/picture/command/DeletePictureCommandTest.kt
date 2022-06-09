package com.wutsi.application.store.endpoint.settings.product.picture.command

import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import import

org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DeletePictureCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @Test
    fun index() {
        // WHEN
        val url = "http://localhost:$port/commands/delete-picture?picture-id=111&product-id=1"
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).deletePicture(111)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/settings/store/product?id=1", action.url)
    }
}
