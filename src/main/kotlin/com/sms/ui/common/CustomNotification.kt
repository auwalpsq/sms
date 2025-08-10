package com.sms.ui.common

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout

fun showSuccess(message: String) {
    Notification().apply {
        val text = Span(message)
        val layout = HorizontalLayout(text)
        layout.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        //layout.alignItems = FlexComponent.Alignment.CENTER
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
        layout.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        //layout.alignItems = FlexComponent.Alignment.CENTER
        layout.setWidthFull()
        add(layout)
        addThemeVariants(NotificationVariant.LUMO_ERROR)
        position = Notification.Position.TOP_STRETCH
        duration = 30000
        open()
    }
}
