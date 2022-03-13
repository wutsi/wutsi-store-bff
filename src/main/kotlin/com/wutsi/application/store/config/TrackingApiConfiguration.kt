package com.wutsi.application.store.config

import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.WutsiTrackingApiStream
import com.wutsi.platform.core.stream.EventStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TrackingApiConfiguration(
    private val eventStream: EventStream
) {
    @Bean
    fun trackingApi(): WutsiTrackingApi =
        WutsiTrackingApiStream(eventStream)
}
