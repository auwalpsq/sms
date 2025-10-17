package com.sms.ui.common

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlinx.coroutines.*

fun showSuccess(message: String) {
    Notification().apply {
        val text = Span(message)
        val layout = HorizontalLayout(text)
        layout.justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER
        layout.setWidthFull()
        add(layout)
        addThemeVariants(NotificationVariant.LUMO_SUCCESS)
        position = Notification.Position.TOP_STRETCH
        duration = 3000
        open()
    }
}

fun showError(message: String) {
    Notification().apply {
        val text = Span(message)
        val layout = HorizontalLayout(text)
        layout.justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER
        layout.setWidthFull()
        add(layout)
        addThemeVariants(NotificationVariant.LUMO_ERROR)
        position = Notification.Position.TOP_STRETCH
        duration = 3000
        open()
    }
}

/**
 * A richer, reusable notification that appears for long-running or real-time events.
 * Example use case: "New Application Submitted", "Application Approved", etc.
 */
fun showInteractiveNotification(
    title: String,
    message: String,
    variant: NotificationVariant = NotificationVariant.LUMO_PRIMARY,
    durationMs: Long = 30_000L
) {
    val ui = UI.getCurrent()
    ui?.access {
        val notification = Notification()
        notification.addThemeVariants(variant)
        notification.position = Notification.Position.TOP_CENTER
        notification.isOpened = true

        val titleText = H3(title)
        val messageText = Span(message)

        val okButton = Button("OK") {
            notification.close()
        }

        val content = VerticalLayout(titleText, messageText, HorizontalLayout(okButton)).apply {
            isSpacing = true
            isPadding = false
        }

        notification.add(content)
        notification.open()

        // Automatically close after duration unless manually closed
        CoroutineScope(Dispatchers.Default).launch {
            delay(durationMs)
            ui.access {
                if (notification.isOpened) {
                    notification.close()
                }
            }
        }
    }
}