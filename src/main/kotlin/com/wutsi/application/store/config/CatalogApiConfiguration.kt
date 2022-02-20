package com.wutsi.application.store.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.shared.service.FeignAcceptLanguageInterceptor
import com.wutsi.ecommerce.catalog.Environment.PRODUCTION
import com.wutsi.ecommerce.catalog.Environment.SANDBOX
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.WutsiCatalogApiBuilder
import com.wutsi.platform.core.security.feign.FeignAuthorizationRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import com.wutsi.platform.core.util.feign.Custom5XXErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
class CatalogApiConfiguration(
    private val authorizationRequestInterceptor: FeignAuthorizationRequestInterceptor,
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val acceptLanguageInterceptor: FeignAcceptLanguageInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment
) {
    @Bean
    fun catalogApi(): WutsiCatalogApi =
        WutsiCatalogApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                authorizationRequestInterceptor,
                acceptLanguageInterceptor
            ),
            errorDecoder = Custom5XXErrorDecoder()
        )

    private fun environment(): com.wutsi.ecommerce.catalog.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            PRODUCTION
        else
            SANDBOX
}
