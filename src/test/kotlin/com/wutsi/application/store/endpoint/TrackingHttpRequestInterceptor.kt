package com.wutsi.application.store.endpoint

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class TrackingHttpRequestInterceptor(
    private val userAgent: String,
    private val referer: String,
    private val ip: String
) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers["User-Agent"] = userAgent
        request.headers["Referer"] = referer
        request.headers["X-Forwarded-For"] = ip
        return execution.execute(request, body)
    }
}
