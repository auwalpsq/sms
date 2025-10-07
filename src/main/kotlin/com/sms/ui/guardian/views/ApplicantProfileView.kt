package com.sms.ui.guardian.views

import com.sms.entities.Applicant
import com.sms.entities.User
import com.sms.entities.Applicant.PaymentStatus
import com.sms.entities.Payment
import com.sms.enums.PaymentCategory
import com.sms.services.AcademicSessionService
import com.sms.services.ApplicantService
import com.sms.services.PaymentService
import com.sms.services.PaymentTypeService
import com.sms.services.PaymentVerificationService
import com.sms.services.PaystackService
import com.sms.services.SchoolClassService
import com.sms.services.StudentService
import com.sms.ui.common.AdmissionLetterView
import com.sms.ui.common.ApplicationFormView
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.ui.components.ApplicationFormDialog
import com.sms.ui.components.PhotoUploadField
import com.sms.ui.guardian.GuardianLayout
import com.sms.util.FormatUtil
import com.sms.util.PaymentUiUtil
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.ClientCallable
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.JavaScript
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.*
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.file.Path
import java.util.UUID

@JavaScript("https://js.paystack.co/v2/inline.js")
@Route(value = "guardian/applicant/:id", layout = GuardianLayout::class)
@PageTitle("Applicant Profile")
@RolesAllowed("GUARDIAN")
class ApplicantProfileView(
    private val applicantService: ApplicantService,
    private val paymentService: PaymentService,
    private val schoolClassService: SchoolClassService,
    private val studentService: StudentService,
    private val paymentTypeService: PaymentTypeService,
    private val paystackService: PaystackService,
    private val academicSessionService: AcademicSessionService,
    private val paymentVerificationService: PaymentVerificationService
) : VerticalLayout(), BeforeEnterObserver {

    private val ui: UI? = UI.getCurrent()
    private val user = SecurityContextHolder.getContext().authentication.principal as User
    private var applicant: Applicant? = null

    private val detailsTab = Tab("Details")
    private val paymentsTab = Tab("Payments")
    private val documentsTab = Tab("Documents")
    private val admissionTab = Tab("Admission")

    private val tabs = Tabs(detailsTab, paymentsTab, admissionTab, documentsTab)

    private val content = VerticalLayout()

    override fun beforeEnter(event: BeforeEnterEvent) {
        val id = event.routeParameters["id"].orElse(null)
        if (id == null) {
            event.forwardTo(GuardianApplicationView::class.java)
            return
        }

        launchUiCoroutine {
            try {
                applicant = applicantService.findById(id.toLong())
                if (applicant?.guardian?.id != user.person?.id) {
                    ui?.withUi { showError("Unauthorized access to applicant") }
                    event.forwardTo(GuardianApplicationView::class.java)
                } else {
                    ui?.withUi {
                        removeAll()
                        buildLayout()
                    }
                }
            } catch (e: Exception) {
                ui?.withUi {
                    showError("Applicant not found")
                    event.forwardTo(GuardianApplicationView::class.java)
                }
            }
        }
    }

    private fun buildLayout() {
        val backButton = Button(VaadinIcon.ARROW_LEFT.create()).apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
            element.setAttribute("title", "Back to Applications")
            addClickListener { ui.ifPresent { it.navigate("guardian/applications") } }
        }

        add(
            HorizontalLayout(backButton, H2("Applicant Profile: ${applicant?.getFullName()}")).apply {
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                setWidthFull()
                justifyContentMode = FlexComponent.JustifyContentMode.START
            }
        )

        // always available
        tabs.removeAll()
        tabs.add(detailsTab)

        if (applicant?.applicationStatus == Applicant.ApplicationStatus.APPROVED) {
            tabs.add(paymentsTab, admissionTab, documentsTab)
        }

        tabs.addSelectedChangeListener { updateContent(tabs.selectedTab) }
        add(tabs, content)
        updateContent(detailsTab)
    }


    private fun updateContent(selected: Tab) {
        content.removeAll()
        when (selected) {
            detailsTab -> showDetails()
            admissionTab -> showAdmission()
            paymentsTab -> showPayment()
            documentsTab -> showDocuments()
        }

    }

    private fun showDetails() {
        val photoField = PhotoUploadField(Path.of("Passport Photo")).apply {
            setPhotoUrl(applicant?.photoUrl)
            upload.isVisible = false
            replaceButton.isVisible = false
            imagePreview.isVisible = true
            imagePreview.width = "150px"
            imagePreview.height = "150px"
            content.defaultHorizontalComponentAlignment = FlexComponent.Alignment.START
        }

        val applicationStatusBadge = Span(applicant?.applicationStatus?.name ?: "-").apply {
            element.themeList.add(
                when (applicant?.applicationStatus) {
                    Applicant.ApplicationStatus.PENDING -> "badge warning"
                    Applicant.ApplicationStatus.APPROVED -> "badge success"
                    Applicant.ApplicationStatus.REJECTED -> "badge error"
                    else -> "badge"
                }
            )
        }

        val formLayout = FormLayout().apply {
            setResponsiveSteps(FormLayout.ResponsiveStep("0", 1))
            addFormItem(Paragraph(applicant?.applicationNumber ?: "-"), "Application No")
            addFormItem(applicationStatusBadge, "Status")
            addFormItem(Paragraph(applicant?.getFullName() ?: "-"), "Full Name")
            addFormItem(Paragraph(applicant?.dateOfBirth?.toString() ?: "-"), "Date of Birth")
            addFormItem(Paragraph(applicant?.currentAge?.toString() ?: "-"), "Age")
            addFormItem(Paragraph(applicant?.gender?.name ?: "-"), "Gender")
            addFormItem(Paragraph(applicant?.submissionDate.toString() ?: "-"), "Submitted On")
            addFormItem(Paragraph(applicant?.intendedClass?.toString() ?: "-"), "Intended Class")
            addFormItem(Paragraph(applicant?.applicationSection?.name ?: "-"), "Section")
            addFormItem(Paragraph(applicant?.guardian?.getFullName() ?: "-"), "Guardian")
            addFormItem(Paragraph(applicant?.guardian?.email ?: "-"), "Guardian Email")
        }

        val leftSide = VerticalLayout().apply {
            isSpacing = true
            isPadding = true
            width = "100%"
            add(photoField, formLayout)
        }

        val dialog = ApplicationFormDialog(
            guardian = applicant!!.guardian!!,
            schoolClassService = schoolClassService,
            onSave = { updated -> applicantService.save(updated).also { reloadApplicant() } },
            onDelete = { applicantService.delete(it.id!!) },
            onChange = { reloadApplicant() }
        )

        when (applicant!!.applicationStatus) {
            Applicant.ApplicationStatus.PENDING -> {
                val editButton = Button("Edit Application", VaadinIcon.EDIT.create()).apply {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    addClickListener { dialog.open(applicant) }
                }
                leftSide.add(editButton)
            }

            Applicant.ApplicationStatus.APPROVED -> {
                if (applicant?.paymentStatus == PaymentStatus.PAID) {
                    if (applicant?.isComplete() == false) {
                        val completeButton = Button("Complete Application", VaadinIcon.EDIT.create()).apply {
                            addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
                            addClickListener { dialog.open(applicant) }
                        }
                        leftSide.add(completeButton)
                    } else {
                        leftSide.add(
                            Paragraph("✅ Application is complete.").apply {
                                style.set("color", "var(--lumo-success-text-color)")
                            }
                        )
                        val appFormLink = Anchor(
                            RouteConfiguration.forApplicationScope().getUrl(
                                ApplicationFormView::class.java,
                                RouteParameters("applicantId", applicant!!.id.toString())
                            ),
                            ""
                        ).apply {
                            setTarget("_blank")
                            element.appendChild(
                                Button("View Application Form", VaadinIcon.FILE_TEXT.create()).apply {
                                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                }.element
                            )
                        }
                        leftSide.add(appFormLink)
                    }
                } else {
                    leftSide.add(
                        Paragraph("⚠ Please complete your application payment before continuing.")
                            .apply { style.set("color", "var(--lumo-error-text-color)") }
                    )
                }
            }

            Applicant.ApplicationStatus.REJECTED -> {
                leftSide.add(Paragraph("❌ Application has been rejected.").apply {
                    style.set("color", "var(--lumo-error-text-color)")
                })
            }
        }

        content.add(leftSide)
    }

    private fun showPayment() {
        launchUiCoroutine {
            val applicationPaymentType = paymentTypeService.findByCategory(PaymentCategory.APPLICATION)

            var payment = if (applicationPaymentType != null) {
                paymentService.findByApplicantIdAndPaymentType(
                    applicantId = applicant!!.id!!,
                    paymentTypeId = applicationPaymentType.id!!
                )
            } else null

            ui?.withUi {
                if (payment == null) {
                    content.add(Paragraph("No application payment has been made."))
                    content.add(Button("Make Application Payment", VaadinIcon.CREDIT_CARD.create()).apply {
                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                        addClickListener {
                            launchUiCoroutine {
                                val paymentType = paymentTypeService.findByCategory(PaymentCategory.APPLICATION)
                                val session = academicSessionService.findCurrent()

                                // Check if payment already exists
                                var payment = paymentService.findByApplicantAndTypeAndSessionAndTerm(
                                    applicant?.id!!, paymentType?.id!!, session?.id!!, session.term
                                )

                                if (payment != null) {
                                    if (payment.status == com.sms.enums.PaymentStatus.SUCCESS) {
                                        ui?.get()?.withUi { showSuccess("✅ You have already paid for this application.") }
                                        return@launchUiCoroutine
                                    }
                                    // Reuse existing reference if pending
                                } else {
                                    // Create new payment
                                    val reference = UUID.randomUUID().toString()
                                    payment = Payment(
                                        applicant = applicant,
                                        guardian = applicant?.guardian,
                                        paymentType = paymentType,
                                        academicSession = session,
                                        term = session.term,
                                        reference = reference,
                                        status = com.sms.enums.PaymentStatus.PENDING
                                    )

                                    paymentService.save(payment)
                                }

                                // Launch Paystack with existing/new reference
                                val reference = payment.reference
                                ui?.get()?.access {
                                    PaymentUiUtil.startPaystackTransaction(
                                        ui = ui.get(),
                                        applicant = applicant!!,
                                        reference = reference,
                                        amount = paymentType.amount,
                                        serverCallbackTarget = this@ApplicantProfileView
                                    )
                                }


                            }
                        }
                    })
                } else {
                    val paymentStatusBadge = Span(payment.status?.name ?: "-").apply {
                        element.themeList.add(
                            when (payment.status) {
                                com.sms.enums.PaymentStatus.SUCCESS -> "badge success"
                                com.sms.enums.PaymentStatus.FAILED -> "badge error"
                                com.sms.enums.PaymentStatus.PENDING -> "badge warning"
                                else -> "badge"
                            }
                        )
                    }

                    val infoLayout = com.vaadin.flow.component.formlayout.FormLayout().apply {
                        setResponsiveSteps(com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1))
                        addFormItem(Paragraph(payment.reference ?: "-"), "Reference")
                        addFormItem(Paragraph(payment.createdAt.toString()), "Date Created")
                        addFormItem(paymentStatusBadge, "Payment Status")
                        addFormItem(Paragraph(payment.term?.name ?: "-"), "Term")
                        addFormItem(Paragraph(FormatUtil.formatCurrency(payment.paymentType?.amount)), "Amount")
                        addFormItem(Paragraph(payment.academicSession?.displaySession ?: "-"), "Academic Session")
                    }

                    content.add(infoLayout)

                    if (payment.status == com.sms.enums.PaymentStatus.SUCCESS) {
                        content.add(Button("Download Receipt", VaadinIcon.FILE_TEXT.create()).apply {
                            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                            addClickListener { showSuccess("TODO: implement receipt download/print") }
                        })
                    } else {
                        content.add(Button("Retry Application Payment", VaadinIcon.CREDIT_CARD.create()).apply {
                            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                            addClickListener {
                                launchUiCoroutine {
                                    val paymentType = paymentTypeService.findByCategory(PaymentCategory.APPLICATION)
                                    val session = academicSessionService.findCurrent()

                                    // Ensure we have a valid payment record
                                    var existingPayment = paymentService.findByApplicantAndTypeAndSessionAndTerm(
                                        applicant?.id!!, paymentType?.id!!, session?.id!!, session.term
                                    )

                                    if (existingPayment == null) {
                                        // Safety: recreate if somehow missing
                                        val reference = UUID.randomUUID().toString()
                                        existingPayment = Payment(
                                            applicant = applicant,
                                            guardian = applicant?.guardian,
                                            paymentType = paymentType,
                                            academicSession = session,
                                            term = session.term,
                                            reference = reference,
                                            status = com.sms.enums.PaymentStatus.PENDING
                                        )
                                        paymentService.save(existingPayment)
                                    }

                                    val reference = existingPayment.reference
                                    ui?.get()?.access {
                                        PaymentUiUtil.startPaystackTransaction(
                                            ui = ui.get(),
                                            applicant = applicant!!,
                                            reference = reference,
                                            amount = paymentType!!.amount,
                                            serverCallbackTarget = this@ApplicantProfileView
                                        )
                                    }
                                }
                            }
                        })

                    }
                }
            }
        }
    }

    private fun showDocuments() {
        content.removeAll()
        val app = applicant ?: return

        val layout = VerticalLayout().apply {
            isSpacing = true
            isPadding = true
            defaultHorizontalComponentAlignment = FlexComponent.Alignment.START
        }

        // Application Form button (only after approval + paid + complete)
        if (app.applicationStatus == Applicant.ApplicationStatus.APPROVED &&
            app.paymentStatus == PaymentStatus.PAID &&
            app.isComplete()
        ) {
            val appFormLink = Anchor(
                RouteConfiguration.forApplicationScope().getUrl(
                    ApplicationFormView::class.java,
                    RouteParameters("applicantId", app.id.toString())
                ),
                ""
            ).apply {
                setTarget("_blank")
                element.appendChild(Button("View Application Form", VaadinIcon.FILE_TEXT.create()).apply {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                }.element)
            }
            layout.add(appFormLink)
        } else {
            val message = when {
                app.applicationStatus != Applicant.ApplicationStatus.APPROVED ->
                    "⚠ Application form is not available until your application is approved."

                app.paymentStatus != PaymentStatus.PAID ->
                    "⚠ Application form is not available until payment is completed."

                !app.isComplete() ->
                    "⚠ Please complete your application before viewing the form."

                else -> "⚠ Application form not available yet."
            }

            layout.add(Paragraph(message).apply {
                style.set("color", "var(--lumo-error-text-color)")
            })
        }

        content.add(layout)
    }

    private fun showAdmission() {
        content.removeAll()
        val app = applicant ?: return

        val rightSide = VerticalLayout().apply {
            add(H2("Admission Details"), Paragraph("Loading..."))
        }

        launchUiCoroutine {
            try {
                val student = studentService.findByApplicantId(app.id!!)
                ui?.withUi {
                    rightSide.removeAll()
                    rightSide.add(H2("Admission Details"))

                    if (student != null) {
                        val studentInfo = FormLayout().apply {
                            setResponsiveSteps(
                                FormLayout.ResponsiveStep("0", 1)
                            )
                            addFormItem(Paragraph(student.admissionNumber), "Admission Number")
                            addFormItem(Paragraph(student.admittedClass.name), "Assigned Class")
                            addFormItem(Paragraph(student.admittedClass.section.name), "Section")
                            addFormItem(Paragraph(student.admittedSession.displaySession), "Academic Session")
                            addFormItem(Paragraph(student.admittedOn?.toString() ?: "-"), "Admitted On")
                        }
                        rightSide.add(studentInfo)

                        if (student.admissionAccepted) {
                            rightSide.add(
                                Paragraph("✅ Admission Accepted on ${student.admissionAcceptedOn ?: "N/A"}").apply {
                                    style.set("color", "var(--lumo-success-text-color)")
                                }
                            )

                            // Add link to admission letter
                            val letterLink = Anchor(
                                RouteConfiguration.forApplicationScope().getUrl(
                                    AdmissionLetterView::class.java,
                                    RouteParameters("studentId", student.id.toString())
                                ),
                                ""
                            ).apply {
                                setTarget("_blank")
                                element.appendChild(
                                    Button("View Admission Letter", VaadinIcon.FILE_TEXT.create()).apply {
                                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                    }.element
                                )
                            }
                            rightSide.add(letterLink)
                        }
                        else {
                            val acceptButton = Button("Accept Admission", VaadinIcon.CHECK.create()).apply {
                                addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS)
                                addClickListener {
                                    launchUiCoroutine {
                                        try {
                                            studentService.acceptAdmission(student.id)
                                            ui?.get()?.withUi {
                                                showSuccess("✅ You have accepted the admission.")
                                                reloadApplicant()
                                            }
                                        } catch (e: Exception) {
                                            ui?.get()?.withUi {
                                                showError("Failed to accept admission: ${e.message}")
                                            }
                                        }
                                    }
                                }
                            }
                            rightSide.add(acceptButton)
                        }
                    } else {
                        rightSide.add(
                            Paragraph("ℹ️ No class has been assigned yet. Please check back later.").apply {
                                style.set("color", "var(--lumo-secondary-text-color)")
                            }
                        )
                    }

                    content.add(rightSide)
                }
            } catch (e: Exception) {
                ui?.withUi { showError("Error loading admission details: ${e.message}") }
            }
        }
    }
    private fun reloadApplicant() {
        launchUiCoroutine {
            applicant = applicantService.findById(applicant!!.id!!)
            ui?.withUi {
                updateContent(tabs.selectedTab)

                // If application is complete, show success message
                if (applicant?.applicationStatus == Applicant.ApplicationStatus.APPROVED &&
                    applicant?.paymentStatus == PaymentStatus.PAID &&
                    applicant?.isComplete() == true
                ) {
                    showSuccess("✅ Your application is now complete.")
                }
            }
        }
    }

    @ClientCallable
    fun paymentSuccess(reference: String) {
        launchUiCoroutine {
            PaymentUiUtil.handlePaymentSuccess(
                reference,
                paystackService,
                paymentVerificationService,
                ui!!
            )
            reloadApplicant()
        }
    }

    @ClientCallable
    fun paymentFailed(reference: String) {
        launchUiCoroutine {
            PaymentUiUtil.handlePaymentFailed(reference, ui!!)
            try {
                paymentService.updateStatus(reference, com.sms.enums.PaymentStatus.FAILED)
            } catch (_: Exception) {}
            reloadApplicant()
        }
    }

    @ClientCallable
    fun paymentCancelled(reference: String) {
        launchUiCoroutine {
            PaymentUiUtil.handlePaymentCancelled(reference, ui!!)
            reloadApplicant()
        }
    }

}