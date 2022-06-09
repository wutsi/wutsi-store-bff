package com.wutsi.application.store.endpoint.settings.section.command

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.settings.section.dto.CreateSectionRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import import

org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateSectionCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        // WHEN
        val url = "http://localhost:$port/commands/create-section"
        val request = CreateSectionRequest(title = "Yo Man")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)

        val req = argumentCaptor<com.wutsi.ecommerce.catalog.dto.CreateSectionRequest>()
        verify(catalogApi).createSection(req.capture())
        assertEquals(request.title, req.firstValue.title)
    }
}
