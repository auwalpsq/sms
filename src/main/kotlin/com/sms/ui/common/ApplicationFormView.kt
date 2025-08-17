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
import com.vaadin.flow.component.html.H2
import jakarta.annotation.security.RolesAllowed

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

        add(header, content, printBtn)
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

        val table = com.vaadin.flow.component.html.NativeTable().apply {
            addClassName("applicant-info")

            addRow("Full Name", "${applicant.firstName} ${applicant.lastName}")
            addRow("Gender", applicant.gender.toString())
            addRow("Date of Birth", applicant.dateOfBirth.toString())
            addRow("Guardian", applicant.guardian?.getFullName() ?: "")
            addRow("Application Status", applicant.applicationStatus.name)
            addRow("Submission Date", applicant.submissionDate?.toString() ?: "")
        }

        content.add(
            Div().apply {
                addClassName("application-section")
                add(H2("Applicant Information"), table)
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
    fun com.vaadin.flow.component.html.NativeTable.addRow(label: String, value: String) {
        val tr = com.vaadin.flow.component.html.NativeTableRow()
        tr.add(com.vaadin.flow.component.html.NativeTableCell(label))
        tr.add(com.vaadin.flow.component.html.NativeTableCell(value))
        this.add(tr)
    }

}