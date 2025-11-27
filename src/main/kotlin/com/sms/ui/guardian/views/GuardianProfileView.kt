package com.sms.ui.guardian.views

import com.sms.entities.Guardian
import com.sms.entities.User
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.ui.components.GuardianDialogForm
import com.sms.ui.guardian.GuardianDashboard
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@Route("guardian/profile", layout = GuardianDashboard::class)
@RolesAllowed("GUARDIAN")
@PageTitle("My Profile")
@Menu(order = 1.0, icon = "vaadin:user", title = "My Profile")
class GuardianProfileView(
    private val guardianService: GuardianService,
    private val applicantService: ApplicantService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private var guardian: Guardian? = null

    private val personalInfo = createSectionLayout()
    private val contactInfo = createSectionLayout()
    private val guardianInfo = createSectionLayout()

    private val editButton = Button("Edit").apply {
        addClickListener { openEditDialog() }
    }

    init {
        setSizeFull()
        isSpacing = true
        isPadding = true

        add(
            H3("My Profile"),
            H4("Personal Information"), personalInfo,
            H4("Contact Information"), contactInfo,
            H4("Guardian Information"), guardianInfo,
            editButton
        )

        loadGuardianData()
    }

    private fun loadGuardianData() {
        val guardianId = getCurrentGuardianId()
        launchUiCoroutine {
            guardian = guardianId?.let { guardianService.findById(it) }
            ui.withUi {
                if (guardian != null) {
                    renderProfile()
                } else {
                    showError("Error loading your profile")
                }
            }
        }
    }

    private fun renderProfile() {
        personalInfo.removeAll()
        contactInfo.removeAll()
        guardianInfo.removeAll()

        guardian?.let { g ->
            // Person fields
            personalInfo.addFormItem(Span(g.firstName ?: ""), "First Name")
            personalInfo.addFormItem(Span(g.middleName ?: ""), "Middle Name")
            personalInfo.addFormItem(Span(g.lastName ?: ""), "Last Name")
            personalInfo.addFormItem(Span(g.gender?.name ?: ""), "Gender")
            personalInfo.addFormItem(Span(g.dateOfBirth?.toString() ?: ""), "Date of Birth")

            // ContactPerson fields
            contactInfo.addFormItem(Span(g.email ?: ""), "Email")
            contactInfo.addFormItem(Span(g.phoneNumber ?: ""), "Phone Number")
            contactInfo.addFormItem(Span(g.address ?: ""), "Address")
            contactInfo.addFormItem(Span(g.city ?: ""), "City")
            contactInfo.addFormItem(Span(g.state ?: ""), "State")

            // Guardian fields
            guardianInfo.addFormItem(Span(g.guardianId ?: ""), "Guardian ID")
            guardianInfo.addFormItem(Span(g.occupation ?: ""), "Occupation")
            guardianInfo.addFormItem(Span(g.employer ?: ""), "Employer")
            guardianInfo.addFormItem(Span(g.alternatePhone ?: ""), "Alternate Phone")
        }
    }

    private fun openEditDialog() {
        guardian?.let { g ->
            val dialog = GuardianDialogForm(
                applicantService = applicantService,
                adminMode = false, // guardian editing their own profile
                isEmailTaken = { false }, // not needed in guardian mode
                onAssignRoles = {_, _ -> },
                loadExistingRoles = {_, -> emptySet()},
                onSave = { updated ->
                    launchUiCoroutine {
                        guardianService.save(updated)
                        ui.withUi {
                            guardian = updated
                            renderProfile()
                            showSuccess("Profile updated successfully")
                        }
                    }
                },
                onDelete = { /* Guardian should not delete themselves */ },
                onChange = { },

            )
            dialog.open(g)
        }
    }

    private fun getCurrentGuardianId(): Long? {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? User
        return user?.person?.id
    }

    private fun createSectionLayout(): FormLayout {
        return FormLayout().apply {
            setResponsiveSteps(
                FormLayout.ResponsiveStep("0", 1),   // mobile → 1 column
                //FormLayout.ResponsiveStep("600px", 2) // tablet/desktop → 2 columns
            )
        }
    }
}