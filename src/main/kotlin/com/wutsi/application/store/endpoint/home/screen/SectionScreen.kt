package com.wutsi.application.store.endpoint.home.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.ProductCard
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.catalog.dto.Section
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.Wrap
import com.wutsi.flutter.sdui.enums.Axis
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/section")
class SectionScreen(
    private val catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider,
    private val sharedUIMapper: SharedUIMapper,
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: Long): Widget {
        val section = catalogApi.getSection(id).section
        val tenant = tenantProvider.get()

        return Screen(
            id = Page.SECTION,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = section.title,
            ),
            child = SingleChildScrollView(
                child = toProductListWidget(section, tenant)
            ),
            bottomNavigationBar = bottomNavigationBar()
        ).toWidget()
    }

    private fun toProductListWidget(
        section: Section,
        tenant: Tenant
    ): WidgetAware {
        val products = catalogApi.searchProducts(
            SearchProductRequest(
                limit = 100,
                sectionId = section.id
            )
        ).products

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = listOf(
                Wrap(
                    children = products.map {
                        Container(
                            width = 180.0,
                            child = ProductCard(
                                model = sharedUIMapper.toProductModel(it, tenant),
                                action = gotoUrl(
                                    url = urlBuilder.build("/product?id=${it.id}")
                                )
                            )
                        )
                    },
                    direction = Axis.Horizontal,
                    spacing = 0.0
                ),
                Container(
                    padding = 10.0,
                    child = Center(
                        child = Text(
                            alignment = TextAlignment.Center,
                            caption = getText("page.section.product-count", arrayOf(products.size.toString()))
                        )
                    )
                ),
            )
        )
    }
}
