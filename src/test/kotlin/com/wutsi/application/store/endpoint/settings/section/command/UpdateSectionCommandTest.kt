package com.wutsi.application.store.endpoint.settings.section.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetSectionResponse
import com.wutsi.ecommerce.catalog.dto.UpdateSectionRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.web.server.LocalServerPort

internal class UpdateSectionCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        // GIVEN
        val section = createSection()
        doReturn(GetSectionResponse(section)).whenever(catalogApi).getSection(any())

        // WHEN
        val url = "http://localhost:$port/commands/update-section?id=${section.id}&sort-order=${section.sortOrder}"
        val request = com.wutsi.application.store.endpoint.settings.section.dto.UpdateSectionRequest(title = "Yo Man")
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)

        val req = argumentCaptor<UpdateSectionRequest>()
        verify(catalogApi).updateSection(eq(section.id), req.capture())
        assertEquals(request.title, req.firstValue.title)
        assertEquals(section.sortOrder, req.firstValue.sortOrder)
    }
}
