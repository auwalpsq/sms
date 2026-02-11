package com.sms.ui.guardian

import com.sms.broadcast.UiBroadcaster
import com.sms.services.AcademicSessionService
import com.sms.ui.common.showInteractiveNotification
import com.sms.ui.layout.MainLayout
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("Guardian Portal")
@Route(value = "guardian", layout = MainLayout::class)
@RolesAllowed("GUARDIAN")
class GuardianDashboard(private val sessionService: AcademicSessionService) :
        Composite<VerticalLayout>() {

    private val ui: UI? = UI.getCurrent()

    override fun initContent(): VerticalLayout {
        val layout = VerticalLayout()
        layout.setSizeFull()
        layout.addClassName("dashboard-root")

        createDashboardContent(layout)
        registerGuardianNotifications()
        return layout
    }

    private fun createDashboardContent(layout: VerticalLayout) {
        val title = H1("Guardian Dashboard").apply { addClassName("dash-title") }
        val sessionSpan = Span("Loading session...")
        sessionSpan.addClassName("dash-subtitle")

        layout.add(title, sessionSpan)

        launchUiCoroutine {
            val session = sessionService.findCurrent()
            ui?.withUi {
                val sessionInfo =
                        if (session != null) {
                            "${session.displaySession} - ${session.term} Term"
                        } else {
                            "No active academic session"
                        }
                sessionSpan.text = sessionInfo
            }
        }
    }

    private fun registerGuardianNotifications() {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? com.sms.entities.User
        val username = user?.username?.trim()?.lowercase() ?: return // 👈 Guardian’s email/username

        val uiRef = ui ?: return
        val session = uiRef.session
        val key = "guardianNotificationListener"

        // Prevent double registration
        if (session.getAttribute(key) != null) return

        val listener: (String, Map<String, Any>) -> Unit = { eventType, data ->
            uiRef.access {
                when (eventType) {
                    "APPLICATION_APPROVED" -> {
                        val applicantName = data["applicantName"] as? String ?: "Unknown"
                        showInteractiveNotification(
                                title = "Application Approved",
                                message = "Congratulations! $applicantName has been approved.",
                                variant = NotificationVariant.LUMO_SUCCESS
                        )
                    }
                    "APPLICATION_REJECTED" -> {
                        val applicantName = data["applicantName"] as? String ?: "Unknown"
                        showInteractiveNotification(
                                title = "Application Rejected",
                                message =
                                        "Unfortunately, $applicantName’s application was rejected.",
                                variant = NotificationVariant.LUMO_ERROR
                        )
                    }
                    "APPLICATION_RESET" -> {
                        val applicantName = data["applicantName"] as? String ?: "Unknown"
                        showInteractiveNotification(
                                title = "Application Reset",
                                message = "$applicantName’s application was reset to pending.",
                                variant = NotificationVariant.LUMO_WARNING
                        )
                    }
                }
            }
        }

        UiBroadcaster.registerForUser(username, listener)
        session.setAttribute(key, listener)

        uiRef.addDetachListener {
            UiBroadcaster.unregisterForUser(username, listener)
            session.setAttribute(key, null)
        }
    }
}
