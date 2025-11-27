package com.sms.ui.guardian.views

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.entities.Guardian
import com.sms.entities.User
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.services.SchoolClassService
import com.sms.ui.components.ApplicationFormDialog
import com.sms.ui.components.PaginationBar
import com.sms.ui.components.SearchBar
import com.sms.ui.guardian.GuardianDashboard
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinSession
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("My Applications")
@Route(value = "guardian/applications", layout = GuardianDashboard::class)
@RolesAllowed("GUARDIAN")
@Menu(order = 2.0, icon = "vaadin:form", title = "Apply for Admission")
class GuardianApplicationView(
    private val applicantService: ApplicantService,
    private val guardianService: GuardianService,
    private val schoolClassService: SchoolClassService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private val user = SecurityContextHolder.getContext().authentication.principal as User
    private val username = user.username

    private var currentGuardian: Guardian? = null
    private var formDialog: ApplicationFormDialog? = null

    private val grid = Grid(Applicant::class.java, false)
    private val searchBar = SearchBar("Search by name or application no...") { query -> loadApplicants(1, query) }
    private val pagination = PaginationBar(pageSize = 10) { page -> loadApplicants(page) }

    private val newApplicationBtn = Button("New Application").apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        addClickListener { formDialog?.open(null) }
    }

    init {
        setSizeFull()
        isSpacing = true
        isPadding = true

        configureGrid()
        configureLayout()

        fetchGuardianAndInitialize()
        registerUpdateListener()
    }

    // --- Initialization ---
    private fun fetchGuardianAndInitialize() {
        launchUiCoroutine {
            currentGuardian = guardianService.findById(user?.person?.id)
            if (currentGuardian != null) {
                ui?.withUi { createFormDialog(); loadApplicants() }
            }
        }
    }

    private fun createFormDialog() {
        formDialog = ApplicationFormDialog(
            guardian = currentGuardian!!,
            schoolClassService = schoolClassService,
            onSave = { applicant -> applicantService.save(applicant) },
            onDelete = { applicant -> applicantService.delete(applicant.id!!) },
            onChange = { loadApplicants(pagination.getCurrentPage()) }
        )
    }

    // --- Layout ---
    private fun configureLayout() {
        val filters = HorizontalLayout(searchBar, newApplicationBtn)
        filters.setWidthFull()
        filters.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        filters.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

        add(filters, grid, pagination)
    }

    // --- Grid ---
    private fun configureGrid() {
        grid.addColumn { it.applicationNumber }.setHeader("Application No.").isAutoWidth = true
        grid.addColumn { it.getFullName() }.setHeader("Full Name").setAutoWidth(true).setFlexGrow(3)
        grid.addColumn { it.gender ?: "" }.setHeader("Gender").isAutoWidth = true
        grid.addColumn { it.dateOfBirth ?: "" }.setHeader("Date of Birth").isAutoWidth = true

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

        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Button("Open Profile").apply {
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE)
                    addClickListener { ui?.get()?.navigate("guardian/applicant/${applicant.id}") }
                }
            }
        ).setHeader("Actions").setAutoWidth(true)

        grid.isAllRowsVisible = true
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        grid.emptyStateText = "No applications found."

    }

    private fun loadApplicants(page: Int = 1, query: String = searchBar.value) {
        val guardianId = currentGuardian?.id ?: return

        launchUiCoroutine {
            val result = if (query.isBlank()) {
                applicantService.getApplicantsByGuardianPaged(
                    guardianId = guardianId,
                    page = page,
                    pageSize = pagination.pageSize
                )
            } else {
                applicantService.searchApplicantsByGuardianPaged(
                    guardianId = guardianId,
                    query = query,
                    page = page,
                    pageSize = pagination.pageSize
                )
            }

            ui?.withUi {
                grid.setItems(result.items)
                pagination.update(result.totalCount)
                grid.recalculateColumnWidths()
            }
        }
    }

    // --- Broadcast Listener ---
    private fun registerUpdateListener() {
        val session = VaadinSession.getCurrent()
        val listenerKey = "guardianApplicationUpdate_$username"

        if (session.getAttribute(listenerKey) == null) {
            val listener: (String, Map<String, Any>) -> Unit = { type, _ ->
                if (type == "APPLICATION_UPDATE") {
                    ui?.access { loadApplicants(pagination.getCurrentPage()) }
                }
            }

            UiBroadcaster.registerForUser(username, listener)
            session.setAttribute(listenerKey, listener)

            ui?.addDetachListener {
                UiBroadcaster.unregisterForUser(username, listener)
                session.setAttribute(listenerKey, null)
            }
        }
    }
}