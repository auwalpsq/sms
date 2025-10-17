package com.sms.broadcast

import com.sms.events.UiNotificationEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArraySet

@Component
object UiBroadcaster {

    // Each listener is a function that takes (type, data)
    private val listeners = CopyOnWriteArraySet<(String, Map<String, Any>) -> Unit>()

    fun register(listener: (String, Map<String, Any>) -> Unit) {
        listeners.add(listener)
    }

    fun unregister(listener: (String, Map<String, Any>) -> Unit) {
        listeners.remove(listener)
    }

    @EventListener
    fun onUiEvent(event: UiNotificationEvent) {
        listeners.forEach { it(event.type, event.data) }
    }
}