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
        grid.addColumn { it.applicationStatus.name }
            .setHeader("Status")

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
                    width = "600px"
                    isModal = true
                    isDraggable = true
                    isResizable = false
                }

                // Header
                val header = H2("Application Review")

                // Applicant Info Layout
                val applicantLayout = FormLayout().apply {
                    addFormItem(Span(applicant.applicationNumber ?: ""), "Application No")
                    addFormItem(Span(applicant.getFullName() ?: ""), "Full Name")
                    addFormItem(Span(applicant.gender?.name ?: ""), "Gender")
                    addFormItem(Span(applicant.dateOfBirth?.toString() ?: ""), "Date of Birth")
                }

                // Guardian Info Layout
                val guardian = applicant.guardian
                val guardianLayout = FormLayout().apply {
                    addFormItem(Span(guardian?.getFullName() ?: ""), "Guardian Name")
                    addFormItem(Span(guardian?.phoneNumber ?: ""), "Phone")
                    addFormItem(Span(guardian?.email ?: ""), "Email")
                    addFormItem(Span(guardian?.address ?: ""), "Address")
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

                dialog.add(
                    VerticalLayout().apply {
                        isSpacing = true
                        isPadding = true
                        add(header)
                        add(H3("Applicant Information"))
                        add(applicantLayout)
                        add(H3("Guardian Information"))
                        add(guardianLayout)
                        add(actions)
                    }
                )

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