package com.wutsi.application.store.endpoint.catalog.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.catalog.dto.SearchProductResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CatalogScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    public val port: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val products = listOf(
            createProductSummary(1),
            createProductSummary(2),
            createProductSummary(3),
            createProductSummary(4)
        )
        doReturn(SearchProductResponse(products)).whenever(catalogApi).searchProducts(any())
    }

    @Test
    fun myCatalog() {
        val url = "http://localhost:$port/catalog"
        assertEndpointEquals("/screens/catalog/catalog-me.json", url)
    }

    @Test
    fun otherCatalog() {
        val accountId = 9L
        val url = "http://localhost:$port/catalog?id=$accountId"
        val account = createAccount(accountId)
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(accountId)
        assertEndpointEquals("/screens/catalog/catalog-other.json", url)
    }
}
