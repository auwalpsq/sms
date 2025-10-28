package com.sms.ui.admin.views

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.services.AcademicSessionService
import com.sms.services.ApplicantService
import com.sms.services.SchoolClassService
import com.sms.services.StudentClassAssignmentService
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
    private val schoolClassService: SchoolClassService,
    private val studentClassAssignmentService: StudentClassAssignmentService,
    private val academicSessionService: AcademicSessionService
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
            addClickListener { UI.getCurrent().navigate("admin/manage-applications") }
        }

        val header = HorizontalLayout(backButton, H2("Review Admission Application")).apply {
            setAlignItems(FlexComponent.Alignment.CENTER)
            width = "100%"
            justifyContentMode = FlexComponent.JustifyContentMode.START
            isSpacing = true
        }

        // Applicant Information (build first; we will add Assigned Class row later if exists)
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
            // Note: Assigned Class row will be appended below if an assignment exists
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

        // Action buttons (status-driven). Do NOT add Assign here — assignment logic handled separately below.
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
            } else if (applicant.applicationStatus == Applicant.ApplicationStatus.APPROVED) {
                when {
                    applicant.paymentStatus == Applicant.PaymentStatus.UNPAID -> {
                        add(Span("⏳ Awaiting payment before class assignment"))
                        // allow reset only if unpaid
                        add(
                            Button("Reset to Pending").apply {
                                addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                                addClickListener { resetApplicant(applicant) }
                            }
                        )
                    }
                    !applicant.isComplete() -> {
                        add(Span("📋 Awaiting guardian to complete profile"))
                        // do not allow reset if paid (we rely on payment check elsewhere)
                    }
                    else -> {
                        // approved + paid + complete → show nothing here re: assignment,
                        // assignment controls will be added below when we inspect student + assignment state
                    }
                }
            } else /* REJECTED */ {
                if (applicant.paymentStatus == Applicant.PaymentStatus.UNPAID) {
                    add(
                        Button("Reset to Pending").apply {
                            addThemeVariants(ButtonVariant.LUMO_CONTRAST)
                            addClickListener { resetApplicant(applicant) }
                        }
                    )
                }
            }
        }

        // -------------------------
        // Assignment handling (correct, session-aware flow)
        // -------------------------
        launchUiCoroutine {
            val currentSession = academicSessionService.findCurrent()
            if (currentSession == null) {
                ui?.withUi { showError("No active academic session configured") }
            } else {
                // Only consider assignment UI when applicant is approved + paid + (optionally) complete
                // (we still allow assign if student record not yet created)
                if (applicant.applicationStatus == Applicant.ApplicationStatus.APPROVED &&
                    applicant.paymentStatus == Applicant.PaymentStatus.PAID) {

                    // find Student (may be null)
                    val student = applicant.id?.let { studentService.findByApplicantId(it) }

                    // find assignment for current session (may be null)
                    val assignment = if (student != null) {
                        studentClassAssignmentService.findAssignment(student.id!!, currentSession.id)
                    } else {
                        null
                    }

                    ui?.withUi {
                        if (assignment != null) {
                            // 🔹 Build readable session label
                            val sessionLabel =
                                "${assignment.academicSession?.displaySession} - ${assignment.academicSession?.term}"

                            // 🔹 Admission acceptance indicator (with color + icon)
                            val accepted = student?.admissionAccepted ?: false
                            val acceptanceLabel = Span().apply {
                                text = if (accepted) "Accepted" else "Not Yet Accepted"
                                element.style.set("color", if (accepted) "green" else "red")
                                element.style.set("font-weight", "bold")
                            }

                            // 🔹 Show Admitted Class and Admission Acceptance in applicant info
                            applicantForm.addFormItem(
                                Span("${assignment.schoolClass?.name} ($sessionLabel)"),
                                "Admitted Class"
                            )
                            applicantForm.addFormItem(acceptanceLabel, "Admission Acceptance")

                            // 🔹 Drop button (only if guardian hasn't accepted)
                            actions.add(
                                Button("Drop Admission").apply {
                                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                                    isEnabled = !accepted
                                    addClickListener {
                                        launchUiCoroutine {
                                            try {
                                                studentClassAssignmentService.deleteAssignment(assignment.id!!)
                                                ui?.get()?.withUi {
                                                    showSuccess("Admission dropped successfully")
                                                    loadApplicant(applicant.id!!)
                                                }
                                            } catch (ex: Exception) {
                                                ui?.get()?.withUi { showError(ex.message ?: "Failed to drop admission") }
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            // 🔹 No assignment yet for current session
                            if (applicant.isComplete()) {
                                actions.add(
                                    Button("Assign Class").apply {
                                        addClickListener { assignClass(applicant) }
                                    }
                                )
                            } else {
                                applicantForm.addFormItem(
                                    Span("Application is paid but missing required details. Complete the profile before assignment."),
                                    "Admission Status"
                                )
                            }
                        }
                    }

                } // end approved+paid block
            } // end currentSession not-null
        }

        // finally add the assembled content to the view
        add(header, H3("Guardian Information"), guardianForm, H3("Applicant Information"), applicantForm, actions)
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
            schoolClassService = schoolClassService,
            onAssigned = { selectedClass ->
                launchUiCoroutine {
                    try {
                        studentService.assignClass(applicant.id!!, selectedClass.id)
                        ui?.withUi {
                            showSuccess("Assigned ${applicant.getFullName()} to ${selectedClass.name}")
                            loadApplicant(applicant.id!!) // refresh view
                        }
                    } catch (ex: Exception) {
                        ui?.withUi { showError("Failed to assign: ${ex.message}") }
                    }
                }
            }
        )
        dialog.open()
    }
}