package com.sms.ui.admin.views

import com.sms.entities.Payment
import com.sms.enums.PaymentStatus
import com.sms.services.PaymentService
import com.sms.util.FormatUtil
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
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

    private val statusFilter = ComboBox<PaymentStatus>("Status")
    private val refreshButton = Button("Refresh", VaadinIcon.REFRESH.create())
    private val ui: UI? = UI.getCurrent()

    init {
        setSizeFull()
        isSpacing = true
        isPadding = true

        configureFilters()
        configureGrid()
        configureToolbar()

        loadPayments()
    }

    private fun configureFilters() {
        statusFilter.setItems(*PaymentStatus.values())
        statusFilter.isClearButtonVisible = true
        statusFilter.addValueChangeListener { loadPayments() }
    }

    private fun configureToolbar() {
        val toolbar = HorizontalLayout(statusFilter, refreshButton).apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        }

        refreshButton.addClickListener { loadPayments() }

        add(toolbar)
    }

    private fun configureGrid() {
        grid.addColumn { it.applicant?.getFullName() ?: "" }.setHeader("Applicant")
        grid.addColumn { it.guardian?.getFullName() ?: "" }.setHeader("Guardian")
        grid.addColumn { it.paymentType?.category ?: "" }.setHeader("Payment Type")
        grid.addColumn { it.academicSession?.displaySession ?: "" }.setHeader("Session")
        grid.addColumn { it.term?.name ?: "" }.setHeader("Term")
        grid.addColumn { FormatUtil.formatCurrency(it.paymentType?.amount ?: 0.0)}.setHeader("Amount")
        grid.addColumn { it.reference ?: "" }.setHeader("Reference")
        grid.addComponentColumn { statusBadge(it.status) }.setHeader("Status")
        grid.addColumn { it.createdAt?.format(formatter) ?: "" }.setHeader("Date")
        grid.setSizeFull()

        add(grid)

        grid.emptyStateText = "No Payment Record Available"
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

    private fun loadPayments() {
        launchUiCoroutine {
            val payments =
                statusFilter.value?.let { paymentService.findByStatus(it) }
                    ?: paymentService.findAll()
            ui?.withUi { grid.setItems(payments) }
        }
    }
}