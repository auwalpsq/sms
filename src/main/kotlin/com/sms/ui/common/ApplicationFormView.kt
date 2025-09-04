package com.sms.ui.common

import com.sms.entities.Applicant
import com.vaadin.flow.component.Html
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.*
import com.sms.services.ApplicantService
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.NativeTable
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.server.streams.DownloadHandler
import com.vaadin.flow.server.streams.DownloadResponse
import jakarta.annotation.security.RolesAllowed
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Route("guardian/application-form/:applicantId")
@RolesAllowed("GUARDIAN")
@PageTitle("Application Form")
class ApplicationFormView(
    private val applicantService: ApplicantService
) : VerticalLayout(), BeforeEnterObserver {

    private var applicant: Applicant? = null
    private val content = Div()
    private val ui: UI? = UI.getCurrent()

    init {
        addClassName("application-form") // main wrapper
        setSizeFull()
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER  // center horizontally
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER

        // Header (Letterhead placeholder)
        val header = Div().apply {
            addClassName("application-letterhead")

            val logo = Image("/images/school-logo.png", "School Logo").apply {
                addClassName("logo")
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
        val appFormFileName = "application-form-${applicant?.applicationNumber ?: applicant?.id}.pdf"

        val appFormHandler = DownloadHandler.fromInputStream({ _ ->
            try {
                val html = """
            <!DOCTYPE html>
            <html xmlns="http://www.w3.org/1999/xhtml">
              <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>Application Form</title>
                <style>
                    /* General Layout */
                    .application-form {
                      width: 800px;
                      margin: 0 auto;
                      padding: 20px 40px;
                      background: white;
                      color: #000;
                      font-family: "Times New Roman", serif;
                      border: 1px solid #ccc;
                      box-shadow: 0 0 5px rgba(0,0,0,0.1);
                      font-size: 15px;
                      line-height: 1.5;
                    }
                    .applicant-info td:first-child, .guardian-info td:first-child, .personal-info td:first-child {
                      font-weight: bold;
                      background: #f9f9f9;
                    }
                    /* Letterhead */
                    .application-letterhead {
                      text-align: center;
                      border-bottom: 2px solid #000;
                      padding-bottom: 15px;
                      margin-bottom: 20px;
                    }

                    .application-letterhead img {
                      height: 80px;
                      margin-bottom: 10px;
                    }

                    .application-letterhead h1 {
                      font-size: 22px;
                      margin: 0;
                      font-weight: bold;
                      text-transform: uppercase;
                    }
                    .info-row {
                      display: flex;
                      gap: 20px;
                      margin-bottom: 20px;
                    }

                    .info-row .application-section {
                      flex: 1;
                    }
                    .application-letterhead p {
                      margin: 2px 0;
                      font-size: 14px;
                    }

                    /* Section Headings */
                    .application-section {
                      margin-top: 5px;
                      font-family: "Times New Roman", serif;
                    }

                    .application-section h2 {
                      font-size: 16px;
                      margin-bottom: 8px;
                      border-bottom: 1px solid #000;
                      padding-bottom: 3px;
                      text-transform: uppercase;
                    }

                    /* Applicant Info Table */
                    .applicant-info, .guardian-info, .personal-info {
                      width: 100%;
                      border-collapse: collapse;
                      margin-top: 10px;
                    }

                    .applicant-info td, .guardian-info td, .personal-info td {
                      padding: 6px 10px;
                      border: 1px solid #ccc;
                      /*border: 1px solid #999;*/
                      font-size: 14px;
                      vertical-align: top;
                    }

                    /* Signature Area */
                    .signature-block {
                      margin-top: 40px;
                      display: flex;
                      justify-content: space-around;
                    }

                    .signature-line {
                      width: 45%;
                      text-align: center;
                      border-top: 1px solid #000;
                      padding-top: 5px;
                      font-size: 14px;
                      flex: 0 0 40%;
                    }

                    /* Print Styles */
                    @media print {
                      body {
                        background: white;
                      }
                      .application-form {
                        box-shadow: none;
                        border: none;
                        width: 100%;
                        padding: 0;
                      }
                      button {
                          display: none !important;
                        }
                    }
                </style>
              </head>
              <body>
                ${this.element.outerHTML}
              </body>
            </html>
        """.trimIndent()

                // Clean & force XHTML
                val document = org.jsoup.Jsoup.parse(html)
                document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
                val xhtml = document.outerHtml()

                // Render PDF
                val baos = ByteArrayOutputStream()
                val renderer = org.xhtmlrenderer.pdf.ITextRenderer()
                renderer.setDocumentFromString(xhtml)
                renderer.layout()
                renderer.createPDF(baos)

                val pdfBytes = baos.toByteArray()
                DownloadResponse(
                    ByteArrayInputStream(pdfBytes),
                    appFormFileName,
                    "application/pdf",
                    pdfBytes.size.toLong()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                DownloadResponse.error(500)
            }
        })

        val downloadForm = Anchor(appFormHandler, "").apply {
            element.setAttribute("download", true) // force as file download
            add(VaadinIcon.FILE.create(), Span(" Download Application Form (PDF)"))
            style.set("display", "inline-flex")
            style.set("gap", "0.4rem")
        }

        add(header, content, downloadForm)
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

        // Personal info
        val personalTable = NativeTable().apply {
            addClassName("personal-info")
            addRow("Full Name", applicant.getFullName())
            addRow("Gender", applicant.gender.toString())
            addRow("Date of Birth", applicant.dateOfBirth.toString())
            addRow("Age", applicant.currentAge)
            addRow("Relationship to Guardian", applicant.relationshipToGuardian.name)
            addRow("Blood Group", applicant.bloodGroup.toString())
            addRow("Genotype", applicant.genotype.toString())
            addRow("Known Allergies", applicant.knownAllergies.toString())
        }

        // Applicant info
        val applicantTable = NativeTable().apply {
            addClassName("applicant-info")
            addRow("Application Number", applicant.applicationNumber ?: "")
            addRow("Intended Class", applicant.intendedClass ?: "")
            addRow("Application Status", applicant.applicationStatus.name)
            addRow("Payment Status", applicant.paymentStatus.name)
            addRow("Submission Date", applicant.submissionDate?.toString() ?: "")
            addRow("Previous School", applicant.previousSchoolName.toString() ?: "Not Specified")
            addRow("Previous Class", applicant.previousClass.toString() ?: "Not Specified")
        }

        // Guardian info
        val guardian = applicant.guardian
        val guardianTable = NativeTable().apply {
            addClassName("guardian-info")
            addRow("Full Name", guardian?.getFullName() ?: "")
            addRow("Email", guardian?.email ?: "")
            addRow("Phone", guardian?.phoneNumber ?: "")
            addRow("Address", guardian?.address ?: "")
        }
        val infoRow = Div().apply {
            addClassName("info-row")
            add(
                Div().apply {
                    addClassName("application-section")
                    add(H2("Personal Information"), personalTable)
                },
                Div().apply {
                    addClassName("application-section")
                    add(H2("Application Information"), applicantTable)
                }
            )
        }
        content.add(
            infoRow,
            Div().apply {
                addClassName("application-section")
                add(H2("Guardian Information"), guardianTable)
            },
            Div().apply {
                addClassName("signature-block")
                add(
                    Div().apply { addClassName("signature-line"); text = "Guardian's Signature" },
                    Div().apply { addClassName("signature-line"); text = "School Official" }
                )
            }
        )
    }

    fun NativeTable.addRow(label: String, value: String) {
        val tr = com.vaadin.flow.component.html.NativeTableRow()
        tr.add(com.vaadin.flow.component.html.NativeTableCell(label))
        tr.add(com.vaadin.flow.component.html.NativeTableCell(value))
        this.add(tr)
    }

}