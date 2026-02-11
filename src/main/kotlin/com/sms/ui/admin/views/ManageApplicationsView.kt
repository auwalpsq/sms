package com.sms.ui.admin.views

import com.sms.services.ApplicantService
import com.sms.services.ContactPersonService
import com.sms.services.GuardianService
import com.sms.ui.layout.MainLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.TabSheet
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("admin/manage-applications", layout = MainLayout::class)
@RolesAllowed("ADMIN")
@PageTitle("Manage Applications")
@Menu(order = 0.0, icon = "vaadin:folder-open", title = "Manage Applications")
class ManageApplicationsView(
        guardianService: GuardianService,
        applicantService: ApplicantService,
        contactPersonService: ContactPersonService
) : VerticalLayout() {

    init {
        setSizeFull()
        val tabSheet = TabSheet()

        val guardianView = GuardiansView(guardianService, applicantService, contactPersonService)
        val applicants = ApplicantsView(applicantService)

        tabSheet.add("Applicants", applicants)
        tabSheet.add("Guardians", guardianView)
        tabSheet.setSizeFull()
        add(tabSheet)
    }
}
