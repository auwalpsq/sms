package com.sms.ui.admin.views

import com.sms.entities.Applicant
import com.sms.services.ApplicantService
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
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
    private val applicantService: ApplicantService
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
                ui?.withUi { renderApplicantPage(loaded) }
            } else {
                ui?.withUi { Notification.show("Applicant not found") }
            }
        }
    }

    private fun renderApplicantPage(applicant: Applicant) {
        removeAll()

        val header = H2("Review Admission Application")

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

            add(
                Button("Assign Class").apply {
                    isEnabled = applicant.isComplete()
                    addClickListener { assignClass(applicant) }
                },
                Button("Back").apply {
                    addClickListener { ui?.get()?.navigate("admin/applicants") }
                }
            )
        }

        add(header, H3("Applicant Information"), applicantForm, H3("Guardian Information"), guardianForm, actions)
    }

    private fun approveApplicant(applicantId: Long) {
        launchUiCoroutine {
            applicantService.approveApplicant(applicantId)
            ui?.withUi {
                Notification.show("Applicant approved")
                ui.navigate("admin/applicants")
            }
        }
    }

    private fun rejectApplicant(applicantId: Long) {
        launchUiCoroutine {
            applicantService.rejectApplicant(applicantId)
            ui?.withUi {
                Notification.show("Applicant rejected")
                ui.navigate("admin/applicants")
            }
        }
    }

    private fun assignClass(applicant: Applicant) {
        // open assign class dialog / page (to implement next)
        Notification.show("Assign class for ${applicant.getFullName()}")
    }
}