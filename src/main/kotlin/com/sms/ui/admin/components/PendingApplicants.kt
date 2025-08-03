package com.sms.ui.admin.components

import com.sms.entities.Applicant
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer

class PendingApplicants(
    private val applicantService: ApplicantService,
    private val guardianService: GuardianService
) : VerticalLayout() {

    private val ui: UI? = UI.getCurrent()
    private val grid = Grid(Applicant::class.java, false)

    init {
        add(H2("Pending Applicants"))
        configureGrid()
        add(grid)
        loadPendingApplicants()
    }

    private fun configureGrid() {
        grid.addColumn { it.applicationNumber }.setHeader("Application No.")
        grid.addColumn { it.getFullName() ?: "N/A" }.setHeader("Applicant Name")
        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.applicationStatus.name).apply {
                    element.setAttribute("theme", when (applicant.applicationStatus) {
                        Applicant.ApplicationStatus.PENDING -> "badge warning"
                        Applicant.ApplicationStatus.APPROVED -> "badge success"
                        Applicant.ApplicationStatus.REJECTED -> "badge error"
                    })
                }
            }
        ).setHeader("Status")

        grid.addComponentColumn { applicant ->
            Button("Open").apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener {
                    showApplicantDialog(applicant)
                }
            }
        }
    }

    private fun loadPendingApplicants() {
        launchUiCoroutine {
            val pending = applicantService.findByStatus(Applicant.ApplicationStatus.PENDING)
            ui?.withUi { grid.setItems(pending) }
        }
    }

    private fun showApplicantDialog(applicant: Applicant) {
        launchUiCoroutine {
            applicant.guardian = guardianService.findById(applicant.guardian?.id)

            ui?.withUi {
                val dialog = Dialog().apply {
                    width = "700px"
                    isModal = true
                }

                val header = H2("Review Admission Application")

                // Applicant Form
                val applicantForm = FormLayout().apply {
                    addFormItem(Span(applicant.applicationNumber ?: ""), "Application No")
                    addFormItem(Span(applicant.getFullName() ?: ""), "Full Name")
                    addFormItem(Span(applicant.gender?.name ?: ""), "Gender")
                    addFormItem(Span(applicant.dateOfBirth?.toString() ?: ""), "Date of Birth")
                    addFormItem(Span(applicant.createdAt?.toString() ?: ""), "Start Date")
                    addFormItem(Span(applicant.updatedAt?.toString() ?: ""), "Last Updated")
                    addFormItem(Span(applicant.paymentStatus?.toString() ?: ""), "Payment Status")
                    addFormItem(Span(applicant.relationshipToGuardian?.toString() ?: ""), "Relationship to Guardian")
                    addFormItem(Span(applicant.previousSchoolName?.toString() ?: ""), "Previous School")
                    addFormItem(Span(applicant.previousClass?.toString() ?: ""), "Prevoius Class")
                    addFormItem(Span(applicant.applicationStatus.name), "Application Status")
                }

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

                val actions = HorizontalLayout().apply {
                    spacing = "true"
                    add(
                        Button("Approve").apply {
                            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                            addClickListener { approveApplicant(applicant, dialog) }
                        },
                        Button("Reject").apply {
                            addThemeVariants(ButtonVariant.LUMO_ERROR)
                            addClickListener { rejectApplicant(applicant, dialog) }
                        },
                        Button("Cancel") {
                            dialog.close()
                        }
                    )
                }

                val layout = VerticalLayout().apply {
                    isSpacing = true
                    isPadding = true
                    add(header)
                    add(H3("Applicant Information"))
                    add(applicantForm)
                    add(H3("Guardian Information"))
                    add(guardianForm)
                    add(actions)
                }

                dialog.add(layout)
                dialog.open()
            }
        }
    }

    private fun approveApplicant(applicant: Applicant, dialog: Dialog) {
        launchUiCoroutine {
            applicant.applicationStatus = Applicant.ApplicationStatus.APPROVED
            applicantService.update(applicant)
            ui?.withUi {
                Notification.show("Applicant approved")
                dialog.close()
                loadPendingApplicants()
            }
        }
    }

    private fun rejectApplicant(applicant: Applicant, dialog: Dialog) {
        launchUiCoroutine {
            applicant.applicationStatus = Applicant.ApplicationStatus.REJECTED
            applicantService.update(applicant)
            ui?.withUi {
                Notification.show("Applicant rejected")
                dialog.close()
                loadPendingApplicants()
            }
        }
    }

}