package com.sms.ui.admin.views

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.entities.User
import com.sms.services.ApplicantService
import com.sms.ui.common.showInteractiveNotification
import com.sms.ui.components.PaginationBar
import com.sms.ui.components.SearchBar
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.server.VaadinSession
import org.springframework.security.core.context.SecurityContextHolder

class ApplicantsView(
    private val applicantService: ApplicantService
) : VerticalLayout() {

    private val user = SecurityContextHolder.getContext().authentication.principal as User
    private val username = user.username
    private val ui = UI.getCurrent()

    private val grid = Grid(Applicant::class.java, false)
    private val statusFilter = ComboBox<Applicant.ApplicationStatus>()
    private lateinit var paginationBar: PaginationBar
    private lateinit var searchBar: SearchBar

    private val pageSize = 10

    init {
        setSizeFull()
        configureGrid()
        configureFilters()
        setupBroadcaster()

        refresh(null, null, 1)
    }

    private fun configureFilters() {
        statusFilter.apply {
            setItems(Applicant.ApplicationStatus.values().toList())
            isClearButtonVisible = true
            placeholder = "All"
        }

        searchBar = SearchBar("Search applicants...") { query ->
            refresh(statusFilter.value, query, 1)
            paginationBar.reset()
        }

        paginationBar = PaginationBar(pageSize) { page ->
            refresh(statusFilter.value, searchBar.value, page)
        }

        statusFilter.addValueChangeListener {
            refresh(statusFilter.value, searchBar.value, 1)
            paginationBar.reset()
        }

        val filterBar = HorizontalLayout(statusFilter, searchBar).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.END
            width = "100%"
            isSpacing = true
            setPadding(false)
            setMargin(false)
        }

        add(filterBar, grid, paginationBar)
    }

    private fun configureGrid() {
        grid.addColumn { it.applicationNumber }.setHeader("Application No.")
            .setAutoWidth(true).setFlexGrow(0)

        grid.addColumn { it.getFullName() ?: "N/A" }
            .setHeader("Applicant Name").setAutoWidth(true)

        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.applicationStatus.name).apply {
                    element.setAttribute(
                        "theme",
                        when (applicant.applicationStatus) {
                            Applicant.ApplicationStatus.PENDING -> "badge warning"
                            Applicant.ApplicationStatus.APPROVED -> "badge success"
                            Applicant.ApplicationStatus.REJECTED -> "badge error"
                        }
                    )
                }
            }
        ).setHeader("Status").setAutoWidth(true)

        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.paymentStatus.name).apply {
                    element.setAttribute(
                        "theme",
                        when (applicant.paymentStatus) {
                            Applicant.PaymentStatus.UNPAID -> "badge error"
                            Applicant.PaymentStatus.PAID -> "badge success"
                            Applicant.PaymentStatus.PARTIALLY_PAID -> "badge contrast"
                        }
                    )
                }
            }
        ).setHeader("Payment").setAutoWidth(true)

        grid.addComponentColumn { applicant ->
            Button("Open").apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener {
                    ui?.get()?.navigate("admin/applicant/${applicant.id}")
                }
            }
        }.setHeader("Action").setAutoWidth(true)

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        grid.isAllRowsVisible = true
    }

    private fun refresh(status: Applicant.ApplicationStatus?, query: String?, page: Int) {
        launchUiCoroutine {
            val pageResult = if (!query.isNullOrBlank()) {
                applicantService.searchApplicantsPaginated(query, status, page, pageSize)
            } else {
                applicantService.findPageByStatus(status, page, pageSize)
            }

            ui?.withUi {
                grid.setItems(pageResult.items)
                paginationBar.update(pageResult.totalCount)
            }
        }
    }

    private fun setupBroadcaster() {
        val session = VaadinSession.getCurrent()
        val listenerKey = "adminListener_$username"

        if (session.getAttribute(listenerKey) == null) {
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
                            // Refresh to show new item
                            refresh(statusFilter.value, searchBar.value, paginationBar.getCurrentPage())
                        }
                    }
                }
            }

            UiBroadcaster.register(listener)
            session.setAttribute(listenerKey, listener)

            ui?.addDetachListener {
                UiBroadcaster.unregister(listener)
                session.setAttribute(listenerKey, null)
            }
        }
    }
}