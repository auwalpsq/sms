package com.sms.ui.common

import com.sms.entities.Applicant
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.*
import com.sms.services.ApplicantService
import com.sms.services.ApplicationFormPdfService
import com.sms.services.PdfExportService
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.NativeTable
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.server.streams.DownloadHandler
import com.vaadin.flow.server.streams.DownloadResponse
import jakarta.annotation.security.RolesAllowed
import java.io.ByteArrayInputStream

@Route("guardian/application-form/:applicantId")
@RolesAllowed("GUARDIAN")
@PageTitle("Application Form")
class ApplicationFormView(
    private val applicantService: ApplicantService,
    private val pdfExportService: PdfExportService,
    private val applicationFormPdfService: ApplicationFormPdfService
) : VerticalLayout(), BeforeEnterObserver {

    private var applicant: Applicant? = null
    private val content = Div()
    private val ui: UI? = UI.getCurrent()

    init {
        addClassName("application-form") // main wrapper
        setSizeFull()
        isPadding = true
        isSpacing = true
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.START

        // Header (Letterhead placeholder)
        val header = Div().apply {
            addClassName("application-letterhead")
            style["text-align"] = "center"

            val logo = Image("images/placeholder.png", "School Logo").apply {
                addClassName("logo")
                style["max-height"] = "80px"
                style["margin-bottom"] = "0.5rem"
            }
            add(
                logo,
                H1("SCHOOL NAME PLACEHOLDER"),
                Paragraph("Address Line 1, City, State"),
                Paragraph("Phone: +234 XXX XXX XXXX | Email: info@school.com")
            )
        }

        // Section container
        content.addClassName("application-section")

        // Print button (can be hidden in print view via CSS)
        val printBtn = Button("Print").apply {
            addClickListener { ui?.get()?.page?.executeJs("window.print();") }
        }
        val downloadBtn = Anchor(
            DownloadHandler.fromInputStream { _ ->
                val model = mapOf("applicant" to applicant!!, "guardian" to applicant!!.guardian!!)
                val pdfBytes = pdfExportService.renderPdf("application-form", model)

                // Using the new DownloadResponse constructor
                DownloadResponse(
                    ByteArrayInputStream(pdfBytes),
                    "application-form-${applicant!!.id}.pdf",
                    "application/pdf",
                    pdfBytes.size.toLong()
                )
            },
            "Download PDF"
        ).apply {
            element.setAttribute("download", true) // ensures browser downloads file
            VaadinIcon.DOWNLOAD.create()
        }
        val imageUrl = this::class.java.getResource("/static/images/placeholder.png")?.toExternalForm()
            ?: throw IllegalStateException("Image not found in resources")
        val princeDownloadBtn = Anchor(
            DownloadHandler.fromInputStream { _ ->
                val model = mapOf(  "applicant" to applicant!!,
                                    "guardian" to applicant!!.guardian!!,
                                    "schoolLogo" to imageUrl
                                )
                val pdfBytes = applicationFormPdfService.renderPdf("application-form", model)

                DownloadResponse(
                    ByteArrayInputStream(pdfBytes),
                    "application-form-${
                                                    applicant!!.applicationNumber
                                                    }-${applicant!!.lastName!!.replace(" ", "_")}-${
                                                    applicant!!.firstName!!.replace(" ", "_")
                                                    }.pdf".lowercase()
                                                    ,
                    "application/pdf",
                    pdfBytes.size.toLong()
                )
            },
            "Download (PrinceXML)"
        ).apply {
            element.setAttribute("download", true)
            VaadinIcon.FILE_PRESENTATION.create()
        }

        add(header, content, printBtn, downloadBtn, princeDownloadBtn)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        val applicantId = event.routeParameters["applicantId"].orElse(null)
        if (applicantId != null) {
            launchUiCoroutine {
                applicant = applicantService.findById(applicantId.toLong())
                ui?.withUi{
                    applicant?.let { showApplicantDetails(it) }
                }
            }
        }
    }

    private fun showApplicantDetails(applicant: Applicant) {
        content.removeAll()

        // Personal info section
        val personalSection = Div().apply {
            addClassName("application-section")
            add(H2("Personal Information"))

            // Full name on its own line
            add(singleField("Full Name", applicant.getFullName(), fullLine = true))

            // Inline row for gender, dob, age
            add(fieldRow(
                "Gender" to applicant.gender.toString(),
                "Date of Birth" to applicant.dateOfBirth.toString(),
                "Age" to applicant.currentAge.toString()
            ))

            add(fieldRow(
                "Relationship to Guardian" to applicant.relationshipToGuardian.name,
                "Blood Group" to applicant.bloodGroup.toString(),
                "Genotype" to applicant.genotype.toString()
            ))

            add(singleField("Known Allergies", applicant.knownAllergies ?: "None"))
        }

        // Applicant info section
        val applicantSection = Div().apply {
            addClassName("application-section")
            add(H2("Application Information"))

            add(fieldRow(
                "Application Number" to (applicant.applicationNumber ?: ""),
                "Intended Class" to (applicant.intendedClass ?: "")
            ))

            add(fieldRow(
                "Application Status" to applicant.applicationStatus.name,
                "Payment Status" to applicant.paymentStatus.name
            ))

            add(singleField("Submission Date", applicant.submissionDate?.toString() ?: ""))
            add(singleField("Previous School", applicant.previousSchoolName ?: "Not Specified"))
            add(singleField("Previous Class", applicant.previousClass ?: "Not Specified"))
        }

        // Guardian info section
        val guardian = applicant.guardian
        val guardianSection = Div().apply {
            addClassName("application-section")
            add(H2("Guardian Information"))

            add(singleField("Full Name", guardian?.getFullName() ?: "", fullLine = true))
            add(fieldRow("Email" to (guardian?.email ?: ""), "Phone" to (guardian?.phoneNumber ?: "")))
            add(singleField("Address", guardian?.address ?: "", fullLine = true))
        }

        // Signature block
        val signatureBlock = Div().apply {
            addClassName("signature-block")
            add(
                Div().apply { addClassName("signature-line"); text = "Guardian's Signature" },
                Div().apply { addClassName("signature-line"); text = "School Official" }
            )
        }

        content.add(personalSection, applicantSection, guardianSection, signatureBlock)
    }

    /**
     * Creates a row with multiple label/value pairs inline.
     */
    private fun fieldRow(vararg pairs: Pair<String, String>): Div {
        return Div().apply {
            addClassName("field-row")
            pairs.forEach { (label, value) ->
                val group = Div().apply { addClassName("field-group") }
                group.add(
                    com.vaadin.flow.component.html.Span("$label:").apply { addClassName("field-label") },
                    com.vaadin.flow.component.html.Span(value).apply { addClassName("field-value") }
                )
                add(group)
            }
        }
    }

    /**
     * Creates a single full-width field.
     */
    private fun singleField(label: String, value: String, fullLine: Boolean = false): Div {
        return Div().apply {
            addClassName("field-single")
            if (fullLine) addClassName("full-line")
            add(
                com.vaadin.flow.component.html.Span("$label:").apply { addClassName("field-label") },
                com.vaadin.flow.component.html.Span(value).apply { addClassName("field-value") }
            )
        }
    }
}