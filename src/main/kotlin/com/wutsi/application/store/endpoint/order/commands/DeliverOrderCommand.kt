package com.wutsi.application.store.endpoint.order.commands

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/deliver-order")
class DeliverOrderCommand(
    private val orderApi: WutsiOrderApi,
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam(name = "id") id: String,
    ): Action {
        orderApi.changeStatus(
            id,
            ChangeStatusRequest(
                status = OrderStatus.DELIVERED.name
            )
        )
        return gotoPreviousScreen()
    }
}
