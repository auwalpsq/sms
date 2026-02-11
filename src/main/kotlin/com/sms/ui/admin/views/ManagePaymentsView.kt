package com.sms.ui.admin.views

import com.sms.services.PaymentService
import com.sms.services.PaymentTypeService
import com.sms.ui.layout.MainLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("admin/manage-payments", layout = MainLayout::class)
@RolesAllowed("ADMIN")
@PageTitle("Manage Payments")
@Menu(order = 3.0, icon = "vaadin:wallet", title = "Manage Payments")
class ManagePaymentsView(
        private val paymentTypeService: PaymentTypeService,
        private val paymentService: PaymentService
) : VerticalLayout() {

    init {
        setSizeFull()
        val tabSheet = TabSheet()

        val paymentTypeView = PaymentsTypeView(paymentTypeService)
        val paymentView = PaymentsView(paymentService) // You would implement this similarly

        tabSheet.add("Payments", paymentView)
        tabSheet.add("Payment Types", paymentTypeView)

        tabSheet.setSizeFull()

        add(tabSheet)
    }
}
