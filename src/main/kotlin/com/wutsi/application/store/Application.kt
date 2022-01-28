package com.wutsi.application.store

import com.wutsi.application.shared.WutsiBffApplication
import com.wutsi.platform.core.WutsiApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@WutsiApplication
@WutsiBffApplication
@SpringBootApplication
@EnableScheduling
class Application

fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}
