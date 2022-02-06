package com.wutsi.application.store.endpoint

import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

abstract class AbstractEndpoint {
    @Autowired
    protected lateinit var messages: MessageSource

    protected fun gotoUrl(
        url: String,
        replacement: Boolean? = null,
        parameters: Map<String, String>? = null
    ) = Action(
        type = ActionType.Route,
        url = url,
        replacement = replacement,
        parameters = parameters
    )

    protected fun gotoPreviousScreen() = Action(
        type = ActionType.Route,
        url = "route:/..",
    )

    protected fun executeCommand(url: String) = Action(
        type = ActionType.Command,
        url = url
    )

    protected fun getText(key: String, args: Array<Any?> = emptyArray()) =
        messages.getMessage(key, args, LocaleContextHolder.getLocale()) ?: key

//    @Autowired
//    protected lateinit var logger: KVLogger
//
//    protected fun createErrorAction(e: Throwable?, messageKey: String): Action {
//        val action = Action(
//            type = Prompt,
//            prompt = Dialog(
//                title = getText("prompt.error.title"),
//                type = Error,
//                message = getText(messageKey)
//            ).toWidget()
//        )
//        log(action, e)
//        return action
//    }
//
//    private fun log(action: Action, e: Throwable?) {
//        logger.add("action_type", action.type)
//        logger.add("action_url", action.url)
//        logger.add("action_prompt_type", action.prompt?.type)
//        logger.add("action_prompt_message", action.prompt?.attributes?.get("message"))
//        if (e != null)
//            logger.setException(e)
//
//        LoggerFactory.getLogger(this::class.java).error("Unexpected error", e)
//    }
//
//    protected fun encodeURLParam(text: String?): String =
//        text?.let { URLEncoder.encode(it, "utf-8") } ?: ""
}
