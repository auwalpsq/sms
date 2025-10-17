package com.sms.ui.admin.views

import com.sms.broadcast.UiBroadcaster
import com.sms.entities.Applicant
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.ui.common.showInteractiveNotification
import com.sms.ui.common.showSuccess
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer

class ApplicantsView(
    private val applicantService: ApplicantService,
    private val guardianService: GuardianService
) : VerticalLayout() {

    private val ui: UI? = UI.getCurrent()
    private val grid = Grid(Applicant::class.java, false)
    private val statusFilter = ComboBox<Applicant.ApplicationStatus>().apply {
        setItems(Applicant.ApplicationStatus.values().toList())
        isClearButtonVisible = true
        placeholder = "All"
        addValueChangeListener { event ->
            val selected = event.value
            refresh(selected)
        }
    }

    init {
        add(H2("Applicants"))
        configureGrid()
        add(statusFilter, grid)
        refresh(null)
        //loadPendingApplicants()

        val listener: (String, Map<String, Any>) -> Unit = { type, data ->
            ui?.access {
                when (type) {
                    "NEW_APPLICATION" -> {
                        val appNumber = data["appNumber"] as? String ?: "Unknown"
                        val status = data["status"] as? String ?: "Pending"
                        showInteractiveNotification(
                            title = "New Application Submitted",
                            message = "Application No: $appNumber\nStatus: $status",
                            variant = NotificationVariant.LUMO_SUCCESS
                        )
                    }
                }
            }
        }

        // Register listener
        UiBroadcaster.register(listener)

        // Unregister when view is detached
        ui?.addDetachListener { UiBroadcaster.unregister(listener) }
    }

    private fun configureGrid() {
        grid.addColumn { it.applicationNumber }.setHeader("Application No.")
            .setAutoWidth(true).setFlexGrow(0)
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
        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.paymentStatus.name).apply {
                    element.setAttribute("theme", when (applicant.paymentStatus) {
                        Applicant.PaymentStatus.UNPAID -> "badge error"
                        Applicant.PaymentStatus.PAID -> "badge success"
                        Applicant.PaymentStatus.PARTIALLY_PAID -> "badge contrast" // or "badge warning"
                    })
                }
            }
        ).setHeader("Payment")

        grid.addComponentColumn { applicant ->
            Button("Open").apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener {
                    ui?.get()?.navigate("admin/applicant/${applicant.id}")
                }
            }
        }

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
    }

    private fun loadApplicants() {
        launchUiCoroutine {
            val applicants = applicantService.findByOptionalStatus(null)
            //val pending = applicantService.findByStatus(Applicant.ApplicationStatus.PENDING)
            ui?.withUi { grid.setItems(applicants) }
        }
    }
    private fun refresh(status: Applicant.ApplicationStatus?) {
        launchUiCoroutine {
            val applicants = applicantService.findByOptionalStatus(status)
            ui?.withUi {
                grid.setItems(applicants)
            }
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
                    addFormItem(Span(applicant.currentAge ?: ""), "Age")
                    addFormItem(Span(applicant.createdAt?.toString() ?: ""), "Start Date")
                    addFormItem(Span(applicant.updatedAt?.toString() ?: ""), "Last Updated")
                    addFormItem(Span(applicant.paymentStatus?.toString() ?: ""), "Payment Status")
                    addFormItem(Span(applicant.relationshipToGuardian?.toString() ?: ""), "Relationship to Guardian")
                    addFormItem(Span(applicant.previousSchoolName?.toString() ?: ""), "Previous School")
                    addFormItem(Span(applicant.previousClass?.toString() ?: ""), "Prevoius Class")
                    addFormItem(Span(applicant.applicationStatus.name), "Application Status")
                    addFormItem(Span(applicant.applicationSection?.toString()), "Application Section")
                    addFormItem(Span(applicant.intendedClass.toString()), "Application Class")
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
            applicantService.approveApplicant(applicant.id)
            val guardianUsername = applicant.guardian?.email ?: return@launchUiCoroutine

            UiBroadcaster.broadcastToUser(
                guardianUsername,
                "APPLICATION_APPROVED",
                mapOf("applicantName" to applicant.getFullName())
            )

            ui?.withUi {
                Notification.show("Applicant approved")
                dialog.close()
                loadApplicants()
            }
        }
    }

    private fun rejectApplicant(applicant: Applicant, dialog: Dialog) {
        launchUiCoroutine {
            applicantService.rejectApplicant(applicant.id)

            val guardianUsername = applicant.guardian?.email ?: return@launchUiCoroutine
            UiBroadcaster.broadcastToUser(
                guardianUsername,
                "APPLICATION_REJECTED",
                mapOf("applicantName" to applicant.getFullName())
            )

            ui?.withUi {
                Notification.show("Applicant rejected")
                dialog.close()
                loadApplicants()
            }
        }
    }

}