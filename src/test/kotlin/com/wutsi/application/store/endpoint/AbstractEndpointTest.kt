package com.wutsi.application.store.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.CategorySummary
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.entity.ProductType
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.Category
import com.wutsi.platform.account.dto.GetAccountResponse
import com.wutsi.platform.account.dto.Phone
import com.wutsi.platform.core.security.SubjectType
import com.wutsi.platform.core.security.SubjectType.USER
import com.wutsi.platform.core.security.spring.SpringAuthorizationRequestInterceptor
import com.wutsi.platform.core.security.spring.jwt.JWTBuilder
import com.wutsi.platform.core.test.TestRSAKeyProvider
import com.wutsi.platform.core.test.TestTokenProvider
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.core.tracing.spring.SpringTracingRequestInterceptor
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.tenant.WutsiTenantApi
import com.wutsi.platform.tenant.dto.GetTenantResponse
import com.wutsi.platform.tenant.dto.Logo
import com.wutsi.platform.tenant.dto.MobileCarrier
import com.wutsi.platform.tenant.dto.PhonePrefix
import com.wutsi.platform.tenant.dto.Tenant
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset
import java.util.UUID
import kotlin.test.assertEquals

abstract class AbstractEndpointTest {
    companion object {
        const val DEVICE_ID = "0000-1111"
        const val ACCOUNT_ID = 77777L
        const val ACCOUNT_NAME = "Ray Sponsible"
        const val TENANT_ID = "1"
    }

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    private lateinit var tracingContext: TracingContext

    @MockBean
    private lateinit var tenantApi: WutsiTenantApi

    @MockBean
    protected lateinit var accountApi: WutsiAccountApi

    @MockBean
    protected lateinit var catalogApi: WutsiCatalogApi

    @Autowired
    private lateinit var messages: MessageSource

    protected lateinit var rest: RestTemplate

    lateinit var traceId: String

    @BeforeEach
    open fun setUp() {
        traceId = UUID.randomUUID().toString()
        doReturn(DEVICE_ID).whenever(tracingContext).deviceId()
        doReturn(traceId).whenever(tracingContext).traceId()
        doReturn(TENANT_ID).whenever(tracingContext).tenantId()

        val tenant = Tenant(
            id = 1,
            name = "test",
            logos = listOf(
                Logo(type = "PICTORIAL", url = "http://www.goole.com/images/1.png")
            ),
            countries = listOf("CM"),
            languages = listOf("en", "fr"),
            numberFormat = "#,###,##0",
            monetaryFormat = "#,###,##0 CFA",
            currency = "XAF",
            currencySymbol = "FCFA",
            dateFormat = "dd MMM yyyy",
            timeFormat = "HH:mm",
            dateTimeFormat = "dd MMM yyyy, HH:mm",
            webappUrl = "https://www.wutsi.me",
            domainName = "www.wutsi.com",
            mobileCarriers = listOf(
                MobileCarrier(
                    code = "mtn",
                    name = "MTN",
                    countries = listOf("CM", "CD"),
                    phonePrefixes = listOf(
                        PhonePrefix(
                            country = "CM",
                            prefixes = listOf("+23795")
                        ),
                    ),
                    logos = listOf(
                        Logo(type = "PICTORIAL", url = "http://www.goole.com/images/mtn.png")
                    )
                ),
                MobileCarrier(
                    code = "orange",
                    name = "ORANGE",
                    countries = listOf("CM"),
                    phonePrefixes = listOf(
                        PhonePrefix(
                            country = "CM",
                            prefixes = listOf("+23722")
                        ),
                    ),
                    logos = listOf(
                        Logo(type = "PICTORIAL", url = "http://www.goole.com/images/orange.png")
                    )
                )
            ),
            product = com.wutsi.platform.tenant.dto.Product(
                defaultPictureUrl = "http://img.com/nopicture.png"
            )
        )
        doReturn(GetTenantResponse(tenant)).whenever(tenantApi).getTenant(any())

        val account = createAccount(ACCOUNT_ID)
        doReturn(GetAccountResponse(account)).whenever(accountApi).getAccount(any())

        rest = createResTemplate()
    }

    private fun createResTemplate(
        scope: List<String> = listOf(
            "user-read",
            "user-manage",
            "payment-method-manage",
            "payment-method-read",
            "payment-manage",
            "payment-read",
            "tenant-read",
        ),
        subjectId: Long = ACCOUNT_ID,
        subjectType: SubjectType = USER
    ): RestTemplate {
        val rest = RestTemplate()
        val tokenProvider = TestTokenProvider(
            JWTBuilder(
                subject = subjectId.toString(),
                name = ACCOUNT_NAME,
                subjectType = subjectType,
                scope = scope,
                keyProvider = TestRSAKeyProvider(),
                admin = false
            ).build()
        )

        rest.interceptors.add(SpringTracingRequestInterceptor(tracingContext))
        rest.interceptors.add(SpringAuthorizationRequestInterceptor(tokenProvider))
        rest.interceptors.add(LanguageClientHttpRequestInterceptor())
        return rest
    }

    protected fun assertEndpointEquals(expectedPath: String, url: String) {
        val request = emptyMap<String, String>()
        val response = rest.postForEntity(url, request, Map::class.java)

        assertJsonEquals(expectedPath, response.body)
    }

    protected fun assertJsonEquals(expectedPath: String, value: Any?) {
        val input = AbstractEndpointTest::class.java.getResourceAsStream(expectedPath)
        val expected = mapper.readValue(input, Any::class.java)

        val writer = mapper.writerWithDefaultPrettyPrinter()

        assertEquals(writer.writeValueAsString(expected).trimIndent(), writer.writeValueAsString(value).trimIndent())
    }

    protected fun getText(key: String, args: Array<Any?> = emptyArray()) =
        messages.getMessage(key, args, LocaleContextHolder.getLocale()) ?: key

    protected fun createFeignException(errorCode: String, downstreamError: ErrorCode? = null) = FeignException.Conflict(
        "",
        Request.create(
            Request.HttpMethod.POST,
            "https://www.google.ca",
            emptyMap(),
            "".toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        ),
        """
            {
                "error":{
                    "code": "$errorCode",
                    "downstreamCode": "$downstreamError"
                }
            }
        """.trimIndent().toByteArray(),
        emptyMap()
    )

    protected fun createProduct(withThumbnail: Boolean = true) = Product(
        id = 1,
        title = "Sample product",
        summary = "Summary of product",
        description = "This is a long description of the product",
        price = 7000.0,
        comparablePrice = 10000.0,
        visible = true,
        category = CategorySummary(id = 1, "Category 1"),
        subCategory = CategorySummary(id = 2, "Category 2"),
        type = ProductType.PHYSICAL.name,
        quantity = 30,
        maxOrder = 5,
        pictures = if (withThumbnail)
            listOf(
                PictureSummary(
                    id = 1,
                    url = "https://www.imag.com/1.png"
                ),
                PictureSummary(
                    id = 2,
                    url = "https://www.imag.com/2.png"
                ),
                PictureSummary(
                    id = 3,
                    url = "https://www.imag.com/3.png"
                )
            )
        else
            emptyList(),
        thumbnail = if (withThumbnail)
            PictureSummary(
                id = 3,
                url = "https://www.imag.com/3.png"
            )
        else
            null
    )

    protected fun createProductSummary(id: Long) = ProductSummary(
        id = id,
        title = "Sample product",
        summary = "Summary of product",
        price = 7000.0,
        comparablePrice = 10000.0,
        thumbnail = PictureSummary(
            id = 3,
            url = "https://www.imag.com/$id.png"
        ),
        categoryId = 1,
        subCategoryId = 2,
        type = ProductType.PHYSICAL.name,
        quantity = 30,
        maxOrder = 5,
    )

    protected fun createAccount(id: Long = ACCOUNT_ID) = Account(
        id = id,
        displayName = "Ray Sponsible",
        country = "CM",
        language = "en",
        status = "ACTIVE",
        phone = Phone(
            id = 1,
            number = "+123766666666$id",
            country = "CM"
        ),
        business = true,
        website = "https://www.google.ca",
        biography = "Short bio to descbribe my business",
        category = Category(
            id = 1000,
            title = "Marketing",
        ),
        timezoneId = "Africa/Douala",
        whatsapp = "+123766666666$id",
    )
}
