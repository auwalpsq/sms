package com.sms.ui.admin.views

import com.sms.entities.PaymentType
import com.sms.services.PaymentTypeService
import com.sms.ui.admin.components.PaymentTypeFormDialog
import com.sms.util.FormatUtil
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon

@PageTitle("Manage Payment Types")
@Route(value = "payment-types", layout = AdminView::class)
class PaymentsTypeView(
    private val paymentTypeService: PaymentTypeService
) : VerticalLayout() {

    private val grid = Grid(PaymentType::class.java, false)
    private val ui: UI? = UI.getCurrent()
    private lateinit var dialog: PaymentTypeFormDialog

    init {
        setSizeFull()
        isSpacing = true
        isPadding = true

        configureGrid()
        val addButton = Button("Add Payment Type", Icon(VaadinIcon.PLUS)).apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addClickListener { dialog.open(null) }
        }
        add(HorizontalLayout(addButton), grid)
        configureFormDialog()
        refreshGrid()
    }

    private fun configureGrid() {
        grid.addColumn(PaymentType::category).setHeader("Description").isAutoWidth = true
        grid.addColumn { FormatUtil.formatCurrency(it.amount) }
            .setHeader("Amount")
            .isAutoWidth = true

        grid.addColumn(PaymentType::description).setHeader("Description").isAutoWidth = true

        grid.asSingleSelect().addValueChangeListener {
            it.value?.let { dialog.open(it) }
        }

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        grid.isAllRowsVisible = true
    }

    private fun configureFormDialog() {
        dialog = PaymentTypeFormDialog(
            onSave = {paymentType -> paymentTypeService.save(paymentType) },
            onDelete = {paymentType -> paymentTypeService.save(paymentType) },
            onChange = {refreshGrid()}
        ).apply {
            configureDialogAppearance()
            width = "60%"
            maxWidth = "600px"
        }
    }

    private fun refreshGrid() {
        launchUiCoroutine {
            val paymentTypes = paymentTypeService.findAll()
            ui?.withUi {
                grid.setItems(paymentTypes)
                grid.recalculateColumnWidths()
            }
        }
    }
}