package com.sms.ui.admin.views

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Payment
import com.sms.enums.PaymentStatus
import com.sms.services.PaymentService
import com.sms.ui.components.PaginationBar
import com.sms.ui.components.SearchBar
import com.sms.util.FormatUtil
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import java.time.format.DateTimeFormatter

@PageTitle("Manage Payments")
@Route(value = "admin/payments", layout = AdminView::class)
class PaymentsView(
    private val paymentService: PaymentService,
) : VerticalLayout() {

    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
    private val grid = Grid(Payment::class.java, false)

    private val statusFilter = ComboBox<PaymentStatus>()
    private val ui: UI? = UI.getCurrent()

    private val searchBar = SearchBar("Search by name, reference") { searchPayments() }
    private val pagination = PaginationBar(pageSize = 10) { loadPage(it) }

    private var currentQuery: String? = null
    private var currentStatus: PaymentStatus? = null

    init {
        setSizeFull()
        isSpacing = true
        isPadding = true

        configureFilters()
        configureGrid()
        configureLayout()

        loadPage(1)

        registerPaymentBroadcastListener()
    }

    private fun configureFilters() {
        statusFilter.setItems(*PaymentStatus.values())
        statusFilter.placeholder = "filter by status"
        statusFilter.isClearButtonVisible = true
        statusFilter.addValueChangeListener {
            currentStatus = it.value
            pagination.reset()
        }
    }

    private fun configureLayout() {
        val filters = HorizontalLayout(searchBar, statusFilter)
        filters.setWidthFull()
        filters.justifyContentMode = FlexComponent.JustifyContentMode.END
        filters.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

        add(filters, grid, pagination)
    }

    private fun configureGrid() {
        grid.addColumn { it.applicant?.getFullName() ?: "" }.setHeader("Applicant").setAutoWidth(true).setFlexGrow(2)
        grid.addColumn { it.paymentType?.category ?: "" }.setHeader("Payment Type").setAutoWidth(true)
        grid.addColumn { it.academicSession?.displaySession ?: "" }.setHeader("Session").setAutoWidth(true)
        grid.addColumn { it.term?.name ?: "" }.setHeader("Term").setAutoWidth(true)
        grid.addColumn { FormatUtil.formatCurrency(it.paymentType?.amount ?: 0.0)}.setHeader("Amount").setAutoWidth(true)
        grid.addColumn { it.reference ?: "" }.setHeader("Reference").setAutoWidth(true)
        grid.addComponentColumn { statusBadge(it.status) }.setHeader("Status").setAutoWidth(true)
        grid.addColumn { it.createdAt?.format(formatter) ?: "" }.setHeader("Date").setAutoWidth(true)

        add(grid)

        grid.emptyStateText = "No Payment Record Available"
        grid.isAllRowsVisible = true
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
    }

    private fun statusBadge(status: PaymentStatus?): Span {
        val badge = Span(status?.name ?: "UNKNOWN")
        badge.element.themeList.add(
            when (status) {
                PaymentStatus.PENDING -> "badge warning"
                PaymentStatus.SUCCESS -> "badge success"
                PaymentStatus.FAILED -> "badge error"
                else -> "badge contrast"
            }
        )
        return badge
    }
    private fun searchPayments() {
        currentQuery = searchBar.value
        pagination.reset()
    }

    private fun loadPage(page: Int) {
        launchUiCoroutine {
            val result = paymentService.findPaged(
                searchQuery = currentQuery,
                status = currentStatus,
                page = page,
                pageSize = pagination.pageSize
            )

            ui?.withUi {
                grid.setItems(result.items)
                pagination.update(result.totalCount)
                grid.recalculateColumnWidths()
            }
        }
    }
    // 👇 Listen for payment events
    private fun registerPaymentBroadcastListener() {
        val session = ui?.session
        val key = "paymentSuccessListener"

        // Prevent duplicate registration
        if (session?.getAttribute(key) != null) return

        val listener: (String, Map<String, Any>) -> Unit = { eventType, data ->
            if (eventType == "PAYMENT_SUCCESS") {
                ui?.access {
                    loadPage(pagination.getCurrentPage())
                }
            }
        }

        UiBroadcaster.register(listener)
        session?.setAttribute(key, listener)

        ui?.addDetachListener {
            UiBroadcaster.unregister(listener)
            session?.setAttribute(key, null)
        }
    }
}