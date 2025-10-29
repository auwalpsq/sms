package com.sms.ui.admin.views

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.entities.User
import com.sms.services.ApplicantService
import com.sms.ui.common.showInteractiveNotification
import com.sms.ui.components.SearchBar
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.server.VaadinSession
import org.springframework.security.core.context.SecurityContextHolder

class ApplicantsView(
    private val applicantService: ApplicantService
) : VerticalLayout() {

    val user = SecurityContextHolder.getContext().authentication.principal as User
    private val username = user.username

    private val ui: UI? = UI.getCurrent()
    private val grid = Grid(Applicant::class.java, false)
    private val statusFilter = ComboBox<Applicant.ApplicationStatus>().apply {
        setItems(Applicant.ApplicationStatus.values().toList())
        isClearButtonVisible = true
        placeholder = "All"
        addValueChangeListener { event ->
            val selected = event.value
            refresh(selected)
        }
    }

    init {
        add(H2("Applicants"))
        setSizeFull()
        configureGrid()
        // Create a search bar that filters by applicant name or application number
        val searchBar = SearchBar("Search applicants...") { query ->
            launchUiCoroutine {
                val applicants = if (query.isBlank()) {
                    applicantService.findByOptionalStatus(statusFilter.value)
                } else {
                    applicantService.searchApplicants(query, statusFilter.value)
                }
                ui?.withUi { grid.setItems(applicants) }
            }
        }

        add(statusFilter, searchBar, grid)
        refresh(null)

        refresh(null)
        //loadPendingApplicants()

        val session = VaadinSession.getCurrent()
        val listenerKey = "adminListener_$username"

        if(session.getAttribute(listenerKey) == null){
            val listener: (String, Map<String, Any>) -> Unit = { type, data ->
                ui?.access {
                    when (type) {
                        "NEW_APPLICATION" -> {
                            val appNumber = data["appNumber"] as? String ?: "Unknown"
                            val status = data["status"] as? String ?: "Pending"
                            showInteractiveNotification(
                                title = "New Application Submitted",
                                message = "Application No: $appNumber\nStatus: $status",
                                variant = NotificationVariant.LUMO_SUCCESS
                            )
                        }
                    }
                }
            }

            // Register listener
            UiBroadcaster.register(listener)
            session.setAttribute(listenerKey, listener)

            // Unregister when view is detached
            ui?.addDetachListener {
                UiBroadcaster.unregister(listener)
                session.setAttribute(listenerKey, null)
            }
        }
    }

    private fun configureGrid() {
        grid.addColumn { it.applicationNumber }.setHeader("Application No.")
            .setAutoWidth(true).setFlexGrow(0)
        grid.addColumn { it.getFullName() ?: "N/A" }.setHeader("Applicant Name")
        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.applicationStatus.name).apply {
                    element.setAttribute("theme", when (applicant.applicationStatus) {
                        Applicant.ApplicationStatus.PENDING -> "badge warning"
                        Applicant.ApplicationStatus.APPROVED -> "badge success"
                        Applicant.ApplicationStatus.REJECTED -> "badge error"
                    })
                }
            }
        ).setHeader("Status")
        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.paymentStatus.name).apply {
                    element.setAttribute("theme", when (applicant.paymentStatus) {
                        Applicant.PaymentStatus.UNPAID -> "badge error"
                        Applicant.PaymentStatus.PAID -> "badge success"
                        Applicant.PaymentStatus.PARTIALLY_PAID -> "badge contrast" // or "badge warning"
                    })
                }
            }
        ).setHeader("Payment")

        grid.addComponentColumn { applicant ->
            Button("Open").apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener {
                    ui?.get()?.navigate("admin/applicant/${applicant.id}")
                }
            }
        }

        grid.isAllRowsVisible = true
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
    }

    private fun loadApplicants() {
        launchUiCoroutine {
            val applicants = applicantService.findByOptionalStatus(null)
            //val pending = applicantService.findByStatus(Applicant.ApplicationStatus.PENDING)
            ui?.withUi { grid.setItems(applicants) }
        }
    }
    private fun refresh(status: Applicant.ApplicationStatus?) {
        launchUiCoroutine {
            val applicants = applicantService.findByOptionalStatus(status)
            ui?.withUi {
                grid.setItems(applicants)
            }
        }
    }

}