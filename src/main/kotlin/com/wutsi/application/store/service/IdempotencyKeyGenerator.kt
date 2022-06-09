package com.wutsi.application.store.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class IdempotencyKeyGenerator {
    fun generate(): String =
        UUID.randomUUID().toString()
}
