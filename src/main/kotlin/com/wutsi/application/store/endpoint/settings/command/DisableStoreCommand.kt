package com.wutsi.application.store.endpoint.settings.command

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.UpdateAccountAttributeRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/disable-store")
class DisableStoreCommand(
    private val accountApi: WutsiAccountApi,
) : AbstractCommand() {
    @PostMapping
    fun index(): Action {
        accountApi.updateAccountAttribute(
            securityContext.currentAccountId(),
            "has-store",
            UpdateAccountAttributeRequest("false")
        )
        return gotoPreviousScreen()
    }
}
