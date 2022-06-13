package com.wutsi.application.store.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.CategorySummary
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.Section
import com.wutsi.ecommerce.catalog.dto.SectionSummary
import com.wutsi.ecommerce.catalog.entity.ProductType
import com.wutsi.ecommerce.order.dto.Address
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.dto.OrderItem
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.ecommerce.shipping.dto.RateSummary
import com.wutsi.ecommerce.shipping.dto.Shipping
import com.wutsi.ecommerce.shipping.dto.ShippingSummary
import com.wutsi.ecommerce.shipping.entity.ShippingType
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.account.dto.AccountSummary
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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @MockBean
    protected lateinit var togglesProvider: TogglesProvider

    @Autowired
    private lateinit var messages: MessageSource

    protected lateinit var rest: RestTemplate

    lateinit var traceId: String

    @BeforeEach
    fun setUp() {
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

    protected fun uploadTo(url: String, filename: String) {
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

    protected fun assertEndpointEquals(expectedPath: String, url: String, request: Map<String, Any> = emptyMap()) {
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

    protected fun createFeignException(
        errorCode: String,
        downstreamError: ErrorCode? = null,
        data: Map<String, Any> = emptyMap()
    ) = FeignException.Conflict(
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
                    "downstreamCode": "$downstreamError",
                    "data": ${toJsonString(data)}
                }
            }
        """.trimIndent().toByteArray(),
        emptyMap()
    )

    private fun toJsonString(data: Map<String, Any>): String =
        ObjectMapper().writeValueAsString(data)

    protected fun createProduct(
        withThumbnail: Boolean = true,
        type: ProductType = ProductType.PHYSICAL,
        accountId: Long = ACCOUNT_ID,
        quantity: Int = 30,
        id: Long = 1,
    ) = Product(
        id = id,
        title = "Sample product",
        summary = "Summary of product",
        description = "This is a long description of the product",
        price = 7000.0,
        comparablePrice = 10000.0,
        visible = true,
        category = CategorySummary(id = 1, "Category 1"),
        subCategory = CategorySummary(id = 2, "Category 2"),
        type = type.name,
        quantity = quantity,
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
            null,
        sections = listOf(
            createSectionSummary(1, "Yo", 1),
            createSectionSummary(2, "Man", 2)
        ),
        accountId = accountId
    )

    protected fun createProductSummary(
        id: Long,
        accountId: Long = ACCOUNT_ID,
        categoryId: Long = 1,
        subCategoryId: Long = 2
    ) = ProductSummary(
        id = id,
        title = "Sample product",
        summary = "Summary of product",
        price = 7000.0,
        comparablePrice = 10000.0,
        thumbnail = PictureSummary(
            id = 3,
            url = "https://www.imag.com/$id.png"
        ),
        categoryId = categoryId,
        subCategoryId = subCategoryId,
        type = ProductType.PHYSICAL.name,
        quantity = 30,
        maxOrder = 5,
        accountId = accountId
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
        cityId = 2222L,
        hasStore = true,
        street = "3030 Linton",
    )

    protected fun createAccountSummary(id: Long = ACCOUNT_ID, displayName: String = "Ray Sponsible") = AccountSummary(
        id = id,
        displayName = displayName,
        country = "CM",
        language = "en",
        status = "ACTIVE",
    )

    fun createShipping(type: ShippingType, enabled: Boolean = true, cityId: Long? = 111L) = Shipping(
        id = 111,
        accountId = 1111,
        type = type.name,
        message = "This is the message to display to customer",
        enabled = enabled,
        rate = 150000.0,
        deliveryTime = 24,
        cityId = cityId,
        country = "CM",
        street = "3030 Linton"
    )

    fun createShippingSummary(type: ShippingType, enabled: Boolean = true, rate: Double = 150000.0) = ShippingSummary(
        id = 111,
        accountId = 1111,
        type = type.name,
        enabled = enabled,
        rate = rate,
        deliveryTime = 24,
        cityId = 11111L,
        country = "CM"
    )

    fun createRateSummary(type: ShippingType, rate: Double = 150000.0) = RateSummary(
        shippingId = 111,
        shippingType = type.name,
        rate = rate,
        deliveryTime = 24,
    )

    fun createOrder(
        shippingAddress: Address? = createAddress(),
        shippingId: Long? = 333,
        status: OrderStatus = OrderStatus.CREATED
    ) = Order(
        id = "111",
        merchantId = 55L,
        totalPrice = 25000.0,
        subTotalPrice = 30000.0,
        savingsAmount = 5000.0,
        currency = "XAF",
        status = status.name,
        reservationId = 777L,
        items = listOf(
            OrderItem(productId = 1, quantity = 10, unitPrice = 100.0, unitComparablePrice = 150.0),
            OrderItem(productId = 2, quantity = 1, unitPrice = 15000.0)
        ),
        shippingAddress = shippingAddress,
        shippingId = shippingId,
        expectedDelivered = OffsetDateTime.of(2020, 1, 3, 15, 0, 0, 0, ZoneOffset.UTC),
        deliveryFees = 1000.0,
        accountId = ACCOUNT_ID,
        created = OffsetDateTime.of(LocalDateTime.of(2022, 4, 14, 0, 0, 0, 0), ZoneOffset.UTC),
    )

    fun createAddress(id: Long = 111L, firstName: String = "Ray") = Address(
        id = id,
        firstName = firstName,
        lastName = "Sponsible",
        country = "CM",
        cityId = 1000,
        street = "180 Rue des Manguier, Bonnapriso",
        email = "ray.sponsible@gmail.com"
    )

    fun createSection(id: Long = 555L, title: String = "Deals", sortOrder: Int = 7) = Section(
        id = id,
        title = title,
        sortOrder = sortOrder,
    )

    fun createSectionSummary(id: Long = 555L, title: String = "Deals", sortOrder: Int = 7, productCount: Int = 0) =
        SectionSummary(
            id = id,
            title = title,
            sortOrder = sortOrder,
        )
}
