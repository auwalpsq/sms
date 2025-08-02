package com.sms.ui.admin.views

import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.ui.admin.components.PendingApplicants
import com.sms.ui.admin.components.Guardians
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("admin/manage-applications", layout = AdminView::class)
@RolesAllowed("ADMIN")
@PageTitle("Manage Applications")
@Menu(order = 0.0, icon = "vaadin:folder-open", title = "Manage Applications")
class ManageApplicationsView(
    guardianService: GuardianService,
    applicantService: ApplicantService
) : VerticalLayout() {

    init {
        setSizeFull()
        val tabSheet = TabSheet()

        val guardianView = Guardians(guardianService)
        val pendingApplicants = PendingApplicants(applicantService, guardianService)

        tabSheet.add("Guardians", guardianView)
        tabSheet.add("Pending Applicants", pendingApplicants)
        tabSheet.setSizeFull()
        add(tabSheet)
    }
}