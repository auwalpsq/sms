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
import com.vaadin.flow.component.html.NativeTable
import com.vaadin.flow.component.orderedlayout.FlexComponent
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