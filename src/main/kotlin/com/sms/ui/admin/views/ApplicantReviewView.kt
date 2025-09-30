package com.sms.ui.admin.views

import com.sms.entities.Applicant
import com.sms.services.ApplicantService
import com.sms.services.SchoolClassService
import com.sms.services.StudentService
import com.sms.ui.admin.components.AssignClassDialog
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("admin/applicant", layout = AdminView::class)
@PageTitle("Review Applicant")
@RolesAllowed("ADMIN")
class ApplicantReviewView(
    private val applicantService: ApplicantService,
    private val studentService: StudentService,
    private val schoolClassService: SchoolClassService
) : VerticalLayout(), HasUrlParameter<Long> {

    private val ui: UI? = UI.getCurrent()
    private var applicant: Applicant? = null

    override fun setParameter(event: BeforeEvent, id: Long?) {
        if (id != null) {
            loadApplicant(id)
        }
    }

    private fun loadApplicant(id: Long) {
        launchUiCoroutine {
            val loaded = applicantService.findById(id)
            if (loaded != null) {
                this@ApplicantReviewView.applicant = loaded
                ui?.withUi { renderApplicantPage(loaded) }
            } else {
                ui?.withUi { showError("Applicant not found") }
            }
        }
    }


    private fun renderApplicantPage(applicant: Applicant) {
        removeAll()

        // 🔹 Back button with arrow icon
        val backButton = Button(Icon(VaadinIcon.ARROW_LEFT)).apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE)
            addClickListener { ui?.get()?.navigate("admin/manage-applications") }
        }

        val header = HorizontalLayout(backButton, H2("Review Admission Application")).apply {
            setAlignItems(FlexComponent.Alignment.CENTER)
            width = "100%"
            justifyContentMode = FlexComponent.JustifyContentMode.START
            isSpacing = true
        }

        // Applicant Information
        val applicantForm = FormLayout().apply {
            addFormItem(Span(applicant.applicationNumber), "Application No")
            addFormItem(Span(applicant.getFullName() ?: ""), "Full Name")
            addFormItem(Span(applicant.gender?.name ?: ""), "Gender")
            addFormItem(Span(applicant.dateOfBirth?.toString() ?: ""), "Date of Birth")
            addFormItem(Span(applicant.currentAge), "Age")
            addFormItem(Span(applicant.createdAt?.toString() ?: ""), "Start Date")
            addFormItem(Span(applicant.updatedAt?.toString() ?: ""), "Last Updated")
            addFormItem(Span(applicant.paymentStatus.name), "Payment Status")
            addFormItem(Span(applicant.relationshipToGuardian.name), "Relationship to Guardian")
            addFormItem(Span(applicant.previousSchoolName ?: ""), "Previous School")
            addFormItem(Span(applicant.previousClass ?: ""), "Previous Class")
            addFormItem(Span(applicant.applicationStatus.name), "Application Status")
            addFormItem(Span(applicant.applicationSection.name), "Application Section")
            addFormItem(Span(applicant.intendedClass ?: ""), "Application Class")
        }

        // Guardian Information
        val guardian = applicant.guardian
        val guardianForm = FormLayout().apply {
            addFormItem(Span(guardian?.getFullName() ?: ""), "Guardian Name")
            addFormItem(Span(guardian?.occupation ?: ""), "Occupation")
            addFormItem(Span(guardian?.employer ?: ""), "Employer")
            addFormItem(Span(guardian?.phoneNumber ?: ""), "Phone")
            addFormItem(Span(guardian?.alternatePhone ?: ""), "Alternate Phone")
            addFormItem(Span(guardian?.email ?: ""), "Email")
            addFormItem(Span(guardian?.address ?: ""), "Address")
            addFormItem(Span(guardian?.city ?: ""), "City")
            addFormItem(Span(guardian?.state ?: ""), "State")
        }

        // Action buttons
        val actions = HorizontalLayout().apply {
            if (applicant.applicationStatus == Applicant.ApplicationStatus.PENDING) {
                add(
                    Button("Approve").apply {
                        addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                        addClickListener { approveApplicant(applicant.id!!) }
                    },
                    Button("Reject").apply {
                        addThemeVariants(ButtonVariant.LUMO_ERROR)
                        addClickListener { rejectApplicant(applicant.id!!) }
                    }
                )
            }

            if (applicant.applicationStatus != Applicant.ApplicationStatus.PENDING &&
                applicant.paymentStatus == Applicant.PaymentStatus.UNPAID) {
                add(
                    Button("Reset to Pending").apply {
                        addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                        addClickListener { resetApplicant(applicant) }
                    }
                )
            }

            add(
                Button("Assign Class").apply {
                    isEnabled = applicant.isComplete()
                    addClickListener { assignClass(applicant) }
                }
            )
        }

        add(header, H3("Applicant Information"), applicantForm, H3("Guardian Information"), guardianForm, actions)
    }

    private fun approveApplicant(applicantId: Long) {
        launchUiCoroutine {
            applicantService.approveApplicant(applicantId)
            val updated = applicantService.findById(applicantId)
            ui?.withUi {
                if (updated != null) {
                    applicant = updated
                    showSuccess("Applicant approved")
                    renderApplicantPage(updated)
                }
            }
        }
    }

    private fun rejectApplicant(applicantId: Long) {
        launchUiCoroutine {
            applicantService.rejectApplicant(applicantId)
            val updated = applicantService.findById(applicantId)
            ui?.withUi {
                if (updated != null) {
                    applicant = updated
                    showError("Applicant rejected")
                    renderApplicantPage(updated)
                }
            }
        }
    }

    private fun resetApplicant(applicant: Applicant) {
        launchUiCoroutine {
            applicantService.resetApplicantToPending(applicant.id)
            val updated = applicantService.findById(applicant.id)
            ui?.withUi {
                if (updated != null) {
                    this@ApplicantReviewView.applicant = updated
                    showSuccess("Application reset to pending")
                    renderApplicantPage(updated)
                }
            }
        }
    }

    private fun assignClass(applicant: Applicant) {
        val dialog = AssignClassDialog(
            applicant = applicant,
            schoolClassService = schoolClassService,   // inject via constructor in view
            studentService = studentService,      // inject via constructor in view

        ) {
            // refresh applicant after assigning
            launchUiCoroutine {
                val updated = applicantService.findById(applicant.id)
                ui?.withUi {
                    if (updated != null) {
                        this@ApplicantReviewView.applicant = updated
                        renderApplicantPage(updated)
                    }
                }
            }
        }
        dialog.open()
    }
}