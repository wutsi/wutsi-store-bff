package com.wutsi.application.store.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.core.security.feign.FeignAuthorizationRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import com.wutsi.platform.core.util.feign.Custom5XXErrorDecoder
import com.wutsi.platform.payment.Environment.PRODUCTION
import com.wutsi.platform.payment.Environment.SANDBOX
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.WutsiPaymentApiBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
class PaymentApiConfiguration(
    private val authorizationRequestInterceptor: FeignAuthorizationRequestInterceptor,
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment,
) {
    @Bean
    fun paymentApi(): WutsiPaymentApi =
        WutsiPaymentApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                authorizationRequestInterceptor
            ),
            errorDecoder = Custom5XXErrorDecoder()
        )

    private fun environment(): com.wutsi.platform.payment.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            PRODUCTION
        else
            SANDBOX
}
