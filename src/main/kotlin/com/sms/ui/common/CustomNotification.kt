package com.sms.ui.common

import com.vaadin.flow.component.notification.Notification

fun showSuccess(message: String) {
    Notification(message, 3000, Notification.Position.TOP_CENTER).apply {
        addClassNames("success-notification")
        //addThemeName("success-notification")
        open()
    }
}

fun showError(message: String) {
    Notification(message, 3000, Notification.Position.TOP_CENTER).apply {
        addClassNames("error-notification")
        //addThemeName("error-notification")
        open()
    }
}
