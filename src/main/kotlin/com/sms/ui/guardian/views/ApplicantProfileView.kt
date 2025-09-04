package com.sms.ui.guardian.views


import com.sms.entities.Applicant
import com.sms.entities.User
import com.sms.entities.Applicant.PaymentStatus
import com.sms.enums.PaymentCategory
import com.sms.services.ApplicantService
import com.sms.services.PaymentService
import com.sms.services.PaymentTypeService
import com.sms.services.SchoolClassService
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.ui.components.ApplicationFormDialog
import com.sms.ui.components.PhotoUploadField
import com.sms.ui.guardian.GuardianLayout
import com.sms.util.FormatUtil
import com.sms.util.buildPdfHtml
import com.sms.util.htmlToPdfBytes
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
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
import com.vaadin.flow.server.streams.DownloadHandler
import com.vaadin.flow.server.streams.DownloadResponse
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder
import java.io.ByteArrayInputStream
import java.nio.file.Path

@Route(value = "guardian/applicant/:id", layout = GuardianLayout::class)
@PageTitle("Applicant Profile")
@RolesAllowed("GUARDIAN")
class ApplicantProfileView(
    private val applicantService: ApplicantService,
    private val paymentService: PaymentService,
    private val schoolClassService: SchoolClassService,
    private val paymentTypeService: PaymentTypeService
) : VerticalLayout(), BeforeEnterObserver {

    private val ui: UI? = UI.getCurrent()
    private val user = SecurityContextHolder.getContext().authentication.principal as User
    private var applicant: Applicant? = null

    private val detailsTab = Tab("Details")
    private val paymentsTab = Tab("Payments")
    private val documentsTab = Tab("Documents")
    private val tabs = Tabs(detailsTab, paymentsTab, documentsTab)

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
            element.setAttribute("title", "Back to Applications") // tooltip
            addClickListener { ui.ifPresent { it.navigate("guardian/applications") } }
        }

        add(
            HorizontalLayout(backButton, H2("Applicant Profile: ${applicant?.getFullName()}")).apply {
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                setWidthFull()
                justifyContentMode = FlexComponent.JustifyContentMode.START
            }
        )

        tabs.addSelectedChangeListener { updateContent(tabs.selectedTab) }
        add(tabs, content)
        updateContent(detailsTab)
    }


    private fun updateContent(selected: Tab) {
        content.removeAll()
        when (selected) {
            detailsTab -> showDetails()
            paymentsTab -> showPayment()
            documentsTab -> showDocuments()
        }
    }

    private fun showDetails() {
        val wrapper = VerticalLayout().apply {
            isSpacing = true
            isPadding = true
            width = "100%"
        }

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

        val formLayout = com.vaadin.flow.component.formlayout.FormLayout().apply {
            setResponsiveSteps(com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1))
            addFormItem(Paragraph(applicant?.applicationNumber ?: "-"), "Application No")
            addFormItem(applicationStatusBadge, "Status")
            addFormItem(Paragraph(applicant?.getFullName() ?: "-"), "Full Name")
            addFormItem(Paragraph(applicant?.dateOfBirth?.toString() ?: "-"), "Date of Birth")
            addFormItem(Paragraph(applicant?.currentAge?.toString() ?: "-"), "Age")
            addFormItem(Paragraph(applicant?.gender?.name ?: "-"), "Gender")
            addFormItem(Paragraph(applicant?.intendedClass?.toString() ?: "-"), "Intended Class")
            addFormItem(Paragraph(applicant?.applicationSection?.name ?: "-"), "Section")
            addFormItem(Paragraph(applicant?.guardian?.getFullName() ?: "-"), "Guardian")
            addFormItem(Paragraph(applicant?.guardian?.email ?: "-"), "Guardian Email")
        }

        val dialog = ApplicationFormDialog(
            guardian = applicant!!.guardian!!,
            schoolClassService = schoolClassService,
            onSave = { updated -> applicantService.save(updated) },
            onDelete = { applicantService.delete(it.id!!) },
            onChange = { reloadApplicant() }
        )

        val editButton = Button("Edit Details", VaadinIcon.EDIT.create()).apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addClickListener { dialog.open(applicant) }
        }

        wrapper.add(photoField, formLayout, editButton)
        content.add(wrapper)
    }



    private fun showPayment() {
        launchUiCoroutine {
            val applicationPaymentType = paymentTypeService.findByCategory(PaymentCategory.APPLICATION)

            val payment = if (applicationPaymentType != null) {
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
                        addClickListener { showSuccess("TODO: integrate Paystack for Application Fee") }
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
                            addClickListener { showSuccess("TODO: integrate Paystack retry for Application Fee") }
                        })
                    }
                }
            }
        }
    }

    private fun showDocuments() {
        content.removeAll()

        // --- Application Form PDF (always available) ---
        val app = applicant ?: return
        val appFormFileName = "application-form-${app.applicationNumber ?: app.id}.pdf"

        val appFormHandler = DownloadHandler.fromInputStream({ event ->
            try {
                val html = buildPdfHtml(app)
                val pdf = htmlToPdfBytes(html)
                DownloadResponse(
                    ByteArrayInputStream(pdf),
                    appFormFileName,
                    "application/pdf",
                    pdf.size.toLong()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                DownloadResponse.error(500)
            }
        })

        val downloadForm = Anchor(appFormHandler, "").apply {
            element.setAttribute("download", true) // force as file download
            add(VaadinIcon.FILE_TEXT.create(), Span(" Download Application Form (PDF)"))
            style.set("display", "inline-flex")
            style.set("gap", "0.4rem")
        }

        content.add(downloadForm)

        // --- Admission Letter PDF (only when approved & paid) ---
//        if (app.applicationStatus == Applicant.ApplicationStatus.APPROVED &&
//            app.paymentStatus == PaymentStatus.PAID) {
//
//            val letterFileName = "admission-letter-${app.applicationNumber ?: app.id}.pdf"
//
//            val letterHandler = DownloadHandler.fromInputStream({ event ->
//                try {
//                    val letterHtml = buildAdmissionLetterHtml(app) // create similar to form HTML
//                    val pdf = htmlToPdfBytes(letterHtml)
//                    DownloadResponse(
//                        ByteArrayInputStream(pdf),
//                        letterFileName,
//                        "application/pdf",
//                        pdf.size.toLong()
//                    )
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    DownloadResponse.error(500)
//                }
//            }, letterFileName)
//
//            val downloadLetter = Anchor(letterHandler, "").apply {
//                element.setAttribute("download", true)
//                add(VaadinIcon.FILE.create(), Span(" Download Admission Letter (PDF)"))
//                style.set("display", "inline-flex")
//                style.set("gap", "0.4rem")
//            }
//
//            content.add(HorizontalLayout(downloadLetter))
//        } else {
//            content.add(Paragraph("Admission Letter not available yet"))
//        }
    }


    private fun reloadApplicant() {
        launchUiCoroutine {
            applicant = applicantService.findById(applicant!!.id!!)
            ui?.withUi { updateContent(tabs.selectedTab) }
        }
    }
}