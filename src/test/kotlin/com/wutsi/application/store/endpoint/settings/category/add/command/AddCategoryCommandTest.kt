package com.wutsi.application.store.endpoint.settings.category.add.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.settings.category.add.dto.AddCategoryRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.catalog.dto.CreateCategoryRequest
import com.wutsi.platform.catalog.dto.CreateCategoryResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class AddCategoryCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/add-category"
    }

    @Test
    fun index() {
        // GIVEN
        val categoryId = 777L
        doReturn(CreateCategoryResponse(categoryId)).whenever(catalogApi).createCategory(any())

        // WHEN
        val request = AddCategoryRequest(
            title = "category-1",
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        kotlin.test.assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<CreateCategoryRequest>()
        verify(catalogApi).createCategory(req.capture())
        kotlin.test.assertEquals(request.title, req.firstValue.title)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)
    }
}
