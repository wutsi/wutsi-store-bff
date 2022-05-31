package com.wutsi.application.store.endpoint.settings.statistics.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetMerchantResponse
import com.wutsi.ecommerce.catalog.dto.Merchant
import com.wutsi.ecommerce.catalog.dto.MerchantSummary
import com.wutsi.ecommerce.catalog.dto.Metrics
import com.wutsi.ecommerce.catalog.dto.SearchMerchantResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsStatisticsScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        // GIVEN
        val item = MerchantSummary(id = 11)
        doReturn(SearchMerchantResponse(listOf(item))).whenever(catalogApi).searchMerchants(any())

        val merchant = Merchant(
            overallMetrics = Metrics(
                totalChats = 1,
                totalOrders = 10,
                totalSales = 1000,
                totalShares = 100,
                totalViews = 10000
            )
        )
        doReturn(GetMerchantResponse(merchant)).whenever(catalogApi).getMerchant(any())

        // WHEN
        val url = "http://localhost:$port/settings/store/statistics"
        assertEndpointEquals("/screens/settings/statistics/index.json", url)
    }

    @Test
    fun empty() {
        // GIVEN
        val item = MerchantSummary(id = 11)
        doReturn(SearchMerchantResponse()).whenever(catalogApi).searchMerchants(any())

        // WHEN
        val url = "http://localhost:$port/settings/store/statistics"
        assertEndpointEquals("/screens/settings/statistics/empty.json", url)
    }
}
