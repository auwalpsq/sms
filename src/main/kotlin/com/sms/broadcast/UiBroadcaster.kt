package com.sms.broadcast

import com.vaadin.flow.component.UI
import java.util.concurrent.CopyOnWriteArraySet

object UiBroadcaster {

    private val listeners = CopyOnWriteArraySet<(String, Map<String, Any>) -> Unit>()
    private val userListeners = mutableMapOf<String, MutableSet<(String, Map<String, Any>) -> Unit>>()

    /** 🔹 Register a general listener (for all users) */
    fun register(listener: (String, Map<String, Any>) -> Unit) {
        listeners.add(listener)
    }

    /** 🔹 Unregister a listener */
    fun unregister(listener: (String, Map<String, Any>) -> Unit) {
        listeners.remove(listener)
    }

    /** 🔹 Register a listener for a specific user (like guardian) */
    fun registerForUser(username: String, listener: (String, Map<String, Any>) -> Unit) {
        userListeners.computeIfAbsent(username) { mutableSetOf() }.add(listener)
    }

    /** 🔹 Unregister user listener */
    fun unregisterForUser(username: String, listener: (String, Map<String, Any>) -> Unit) {
        userListeners[username]?.remove(listener)
    }

    /** 🔹 Broadcast a message to all connected UIs */
    fun broadcast(eventType: String, data: Map<String, Any>) {
        for (listener in listeners) {
            listener(eventType, data)
        }
    }

    /** 🔹 Broadcast a message only to a specific user (e.g. guardian) */
    fun broadcastToUser(username: String, eventType: String, data: Map<String, Any>) {
        userListeners[username]?.forEach { listener ->
            listener(eventType, data)
        }
    }
}