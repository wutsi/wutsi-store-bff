package com.wutsi.application.store.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.WutsiOrderApiBuilder
import com.wutsi.platform.core.security.feign.FeignAuthorizationRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import com.wutsi.platform.core.util.feign.Custom5XXErrorDecoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
class OrderApiConfiguration(
    private val authorizationRequestInterceptor: FeignAuthorizationRequestInterceptor,
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment
) {
    @Bean
    fun orderApi(): WutsiOrderApi =
        WutsiOrderApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                authorizationRequestInterceptor,
            ),
            errorDecoder = Custom5XXErrorDecoder()
        )

    private fun environment(): com.wutsi.ecommerce.order.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            com.wutsi.ecommerce.order.Environment.PRODUCTION
        else
            com.wutsi.ecommerce.order.Environment.SANDBOX
}
