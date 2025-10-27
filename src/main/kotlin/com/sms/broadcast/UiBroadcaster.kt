package com.sms.broadcast

import java.util.concurrent.CopyOnWriteArraySet

object UiBroadcaster {

    private val listeners = CopyOnWriteArraySet<(String, Map<String, Any>) -> Unit>()
    private val userListeners = mutableMapOf<String, CopyOnWriteArraySet<(String, Map<String, Any>) -> Unit>>()

    fun register(listener: (String, Map<String, Any>) -> Unit) {
        listeners.add(listener)
    }

    fun unregister(listener: (String, Map<String, Any>) -> Unit) {
        listeners.remove(listener)
    }

    fun registerForUser(username: String, listener: (String, Map<String, Any>) -> Unit) {
        val normalized = username.trim().lowercase()
        userListeners.computeIfAbsent(normalized) { CopyOnWriteArraySet() }.add(listener)
    }

    fun unregisterForUser(username: String, listener: (String, Map<String, Any>) -> Unit) {
        val normalized = username.trim().lowercase()
        userListeners[normalized]?.remove(listener)
    }

    fun broadcast(eventType: String, data: Map<String, Any>) {
        listeners.forEach { it(eventType, data) }
    }

    fun broadcastToUser(username: String, eventType: String, data: Map<String, Any>) {
        val normalized = username.trim().lowercase()
        val listenersForUser = userListeners[normalized]
        if (listenersForUser.isNullOrEmpty()) {
            println("⚠️ No listeners registered for user: $normalized")
        } else {
            println("📢 Broadcasting '$eventType' to ${listenersForUser.size} listener(s) for $normalized")
            listenersForUser.forEach { it(eventType, data) }
        }
    }
}
