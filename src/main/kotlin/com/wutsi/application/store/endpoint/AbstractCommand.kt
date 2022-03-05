package com.wutsi.application.store.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.ecommerce.order.error.ErrorURN
import com.wutsi.platform.core.error.ErrorResponse
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractCommand : AbstractEndpoint() {
    @Autowired
    private lateinit var mapper: ObjectMapper

    protected fun getErrorText(ex: FeignException): String {
        try {
            val response = mapper.readValue(ex.contentUTF8(), ErrorResponse::class.java)
            val code = response.error.code
            if (code == ErrorURN.PRODUCT_AVAILABILITY_ERROR.urn) {
                return getText("error.order.PRODUCT_AVAILABILITY_ERROR")
            } else if (code == com.wutsi.platform.payment.error.ErrorURN.TRANSACTION_FAILED.urn) {
                try {
                    val downstreamCode = response.error.downstreamCode
                    return getText("error.payment.$downstreamCode")
                } catch (ex: Exception) {
                    return getText("error.payment")
                }
            }
        } catch (ex: Exception) {
        }

        return getText("error.unexpected")
    }
}
