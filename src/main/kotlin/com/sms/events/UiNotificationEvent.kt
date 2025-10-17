package com.sms.events

import org.springframework.context.ApplicationEvent

class UiNotificationEvent(
    source: Any,
    val type: String,                // e.g. "APPLICATION_UPDATED", "NEW_APPLICATION"
    val data: Map<String, Any> = emptyMap() // Flexible payload
) : ApplicationEvent(source)
