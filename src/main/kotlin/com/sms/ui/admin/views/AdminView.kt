package com.sms.ui.admin.views

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

@PageTitle("Admin Dashboard")
@Route(value = "admin", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class AdminView(private val sessionService: AcademicSessionService) : Composite<VerticalLayout>() {

    private val ui: UI? = UI.getCurrent()

    override fun initContent(): VerticalLayout {
        val layout = VerticalLayout()
        layout.setSizeFull()
        layout.addClassName("dashboard-root") // Use the CSS class for padding/gap

        createDashboardContent(layout)
        registerGlobalAdminNotifications()
        return layout
    }

    private fun createDashboardContent(layout: VerticalLayout) {
        // Dashboard Title
        val title = H1("Dashboard").apply { addClassName("dash-title") }

        // Session Info
        val sessionSpan = Span("Loading session...")
        sessionSpan.addClassName("dash-subtitle")

        layout.add(title, sessionSpan)

        launchUiCoroutine {
            val session = sessionService.findCurrent()
            ui?.withUi {
                val info =
                        if (session != null) {
                            "${session.displaySession} - ${session.term} Term"
                        } else {
                            "No active academic session"
                        }
                sessionSpan.text = info
            }
        }

        // Here we can add the "Stats" grid and "Recent Activity" parts
        // For now, we will add placeholders matching the CSS structure

        val statsGrid =
                com.vaadin.flow.component.html.Div().apply {
                    addClassName("dash-stats-grid")
                    // Example Stat Card 1
                    add(createStatCard("Total Students", "1,204", "12%", true))
                    // Example Stat Card 2
                    add(createStatCard("Applicants", "45", "5%", true))
                    // Example Stat Card 3
                    add(createStatCard("Staff", "89", "0%", true))
                    // Example Stat Card 4
                    add(createStatCard("Revenue", "₦1.2M", "8%", true))
                }

        layout.add(statsGrid)
    }

    private fun createStatCard(
            label: String,
            value: String,
            percent: String,
            isUp: Boolean
    ): com.vaadin.flow.component.html.Div {
        val card =
                com.vaadin.flow.component.html.Div().apply {
                    addClassName("dash-stat-card")
                    addClassName("stat-animate")
                }

        // Top Row
        val topRow = com.vaadin.flow.component.html.Div().apply { addClassName("stat-top-row") }

        val icon =
                com.vaadin.flow.component.html.Div().apply {
                    addClassName("stat-icon")
                    addClassName("icon-total") // Default color class
                    // Vaadin Icon
                    add(
                            com.vaadin.flow.component.icon.Icon(
                                            com.vaadin.flow.component.icon.VaadinIcon.GROUP
                                    )
                                    .apply { addClassName("ph") }
                    )
                }

        val percentWrap =
                com.vaadin.flow.component.html.Div().apply {
                    addClassName("percent-wrap")
                    // Badge
                    val badge =
                            com.vaadin.flow.component.html.Div().apply {
                                addClassName("stat-percent-badge-text")
                                text = "+$percent"
                            }
                    add(badge)
                }

        topRow.add(icon, percentWrap)

        // Body
        val body = com.vaadin.flow.component.html.Div().apply { addClassName("stat-body") }
        val valSpan =
                com.vaadin.flow.component.html.Span(value).apply { addClassName("stat-value") }
        val labelSpan =
                com.vaadin.flow.component.html.Span(label).apply { addClassName("stat-label") }

        body.add(valSpan, labelSpan)

        card.add(topRow, body)
        return card
    }

    private fun registerGlobalAdminNotifications() {
        val uiRef = ui ?: return
        val session = uiRef.session
        val listenerKey = "adminGlobalNotificationListener"

        // Prevent double registration
        if (session.getAttribute(listenerKey) != null) return

        val listener: (String, Map<String, Any>) -> Unit = { eventType, data ->
            uiRef.access {
                when (eventType) {
                    "APPLICATION_SUBMITTED" -> {
                        val guardian = data["guardianEmail"] as? String ?: "Unknown"
                        showInteractiveNotification(
                                title = "New Admission Application",
                                message = "A new admission application was submitted by $guardian.",
                                variant = NotificationVariant.LUMO_PRIMARY
                        )
                    }
                    "PAYMENT_SUCCESS" -> {
                        showInteractiveNotification(
                                title = "Payment Notification",
                                message =
                                        "${data["message"]} with reference ${data["reference"]} at ${data["timestamp"]}",
                                variant = NotificationVariant.LUMO_SUCCESS
                        )
                    }
                }
            }
        }

        // ✅ Register once per session
        UiBroadcaster.register(listener)
        session.setAttribute(listenerKey, listener)

        // ✅ Clean up when UI closes
        uiRef.addDetachListener {
            UiBroadcaster.unregister(listener)
            session.setAttribute(listenerKey, null)
        }
    }
}
