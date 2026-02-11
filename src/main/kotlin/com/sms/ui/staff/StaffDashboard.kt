package com.sms.ui.staff

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

@PageTitle("Staff Portal")
@Route(value = "staff", layout = MainLayout::class)
@RolesAllowed("STAFF") // Admin can also enter staff dashboard
class StaffDashboard(private val sessionService: AcademicSessionService) :
        Composite<VerticalLayout>() {

    private val ui: UI? = UI.getCurrent()

    override fun initContent(): VerticalLayout {
        val layout = VerticalLayout()
        layout.setSizeFull()
        layout.addClassName("dashboard-root")

        createDashboardContent(layout)
        registerStaffNotifications()
        return layout
    }

    private fun createDashboardContent(layout: VerticalLayout) {
        val title = H1("Staff Dashboard").apply { addClassName("dash-title") }
        val sessionSpan = Span("Loading session...")
        sessionSpan.addClassName("dash-subtitle")

        layout.add(title, sessionSpan)

        launchUiCoroutine {
            val session = sessionService.findCurrent()
            ui?.withUi {
                val sessionInfo =
                        session?.let { "${it.displaySession} - ${it.term} Term" }
                                ?: "No active academic session"
                sessionSpan.text = sessionInfo
            }
        }
    }

    private fun registerStaffNotifications() {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? com.sms.entities.User
        val username = user?.username?.lowercase()?.trim() ?: return

        val uiRef = ui ?: return
        val session = uiRef.session
        val key = "staffNotificationListener"

        if (session.getAttribute(key) != null) return

        val listener: (String, Map<String, Any>) -> Unit = { eventType, data ->
            uiRef.access {
                when (eventType) {
                    "GENERAL_ALERT" ->
                            showInteractiveNotification(
                                    title = "Staff Alert",
                                    message = data["message"] as? String
                                                    ?: "New staff notification.",
                                    variant = NotificationVariant.LUMO_CONTRAST
                            )
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
