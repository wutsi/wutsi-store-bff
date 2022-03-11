package com.wutsi.application.store.endpoint.settings.command

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.CreateProductRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.account.dto.UpdateAccountAttributeRequest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DisableStoreCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        // WHEN
        val url = "http://localhost:$port/commands/disable-store"
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<CreateProductRequest>()
        verify(accountApi).updateAccountAttribute(
            eq(ACCOUNT_ID),
            eq("has-store"),
            eq(UpdateAccountAttributeRequest("false"))
        )
    }
}
