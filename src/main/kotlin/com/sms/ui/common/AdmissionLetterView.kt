package com.sms.ui.common

import com.sms.entities.Student
import com.sms.services.ApplicationFormPdfService
import com.sms.services.StudentService
import com.sms.ui.components.SchoolHeader
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.*
import com.vaadin.flow.server.streams.DownloadHandler
import com.vaadin.flow.server.streams.DownloadResponse
import jakarta.annotation.security.RolesAllowed
import java.io.ByteArrayInputStream

@Route("guardian/admission-letter/:studentId")
@RolesAllowed("GUARDIAN")
@PageTitle("Admission Letter")
class AdmissionLetterView(
    private val studentService: StudentService,
    private val applicationFormPdfService: ApplicationFormPdfService
) : VerticalLayout(), BeforeEnterObserver {

    private var student: Student? = null
    private val ui = UI.getCurrent()
    private val content = Div()

    init {
        addClassName("admission-letter")
        setSizeFull()
        isPadding = true
        isSpacing = true
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER

        val header = SchoolHeader()
        content.addClassName("admission-content")

        val imageUrl = this::class.java.getResource("/static/images/placeholder.png")?.toExternalForm()
            ?: throw IllegalStateException("Image not found in resources")

        // --- Buttons ---
        val printBtn = Button("Print", VaadinIcon.PRINT.create()).apply {
            addClickListener { ui?.get()?.page?.executeJs("window.print();") }
        }

        val downloadBtn = Anchor(
            DownloadHandler.fromInputStream { _ ->
                val model = mapOf(
                    "student" to student!!,
                    "applicant" to student!!.applicant,
                    "guardian" to student!!.applicant.guardian,
                    "schoolClass" to student!!.admittedClass,
                    "session" to student!!.admittedSession,
                    "schoolLogo" to imageUrl
                )
                val pdfBytes = applicationFormPdfService.renderPdf("admission-letter", model)
                DownloadResponse(
                    ByteArrayInputStream(pdfBytes),
                    "admission-letter-${student!!.admissionNumber}.pdf",
                    "application/pdf",
                    pdfBytes.size.toLong()
                )
            },
            ""
        ).apply {
            element.setAttribute("download", true)
            add(Button("Download PDF", VaadinIcon.DOWNLOAD.create()))
        }

        val btnBar = HorizontalLayout(printBtn, downloadBtn).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isSpacing = true
        }

        add(header, content, btnBar)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        val studentId = event.routeParameters["studentId"].orElse(null)
        if (studentId != null) {
            launchUiCoroutine {
                try {
                    val found = studentService.findById(studentId.toLong())
                    ui?.withUi {
                        if (found != null) {
                            student = found
                            showLetter(found)
                        } else {
                            content.text = "Student not found"
                        }
                    }
                } catch (e: Exception) {
                    ui?.withUi { content.text = "Error: ${e.message}" }
                }
            }
        } else {
            content.text = "Missing student ID"
        }
    }

    private fun showLetter(student: Student) {
        content.removeAll()

        val applicant = student.applicant
        val guardian = applicant.guardian
        val schoolClass = student.admittedClass
        val session = student.admittedSession

        val title = H1("Admission Letter")
        val sub = H2("Admission Offer for ${applicant.getFullName()}")

        val intro = Paragraph().apply {
            add(Text("Dear "))
            add(spanUnderline(guardian?.getFullName() ?: "Parent/Guardian"))
            add(Text(","))
            add(
                Text(
                    "We are delighted to inform you that "
                )
            )
            add(spanUnderline(applicant.getFullName()))
            add(
                Text(
                    " has been offered admission into "
                )
            )
            add(spanUnderline(schoolClass.name))
            add(
                Text(" for the ")
            )
            add(spanUnderline(session.displaySession))
            add(Text(" academic session."))
        }

        val details = Div().apply {
            addClassNames("details")
            add(detailParagraph("Admission Number", student.admissionNumber))
            add(detailParagraph("Application Number", applicant.applicationNumber ?: "-"))
            add(detailParagraph("Admitted Class", schoolClass.name))
            add(detailParagraph("Section", schoolClass.section?.name ?: "-"))
            add(detailParagraph("Session", session.displaySession))
            add(detailParagraph("Date of Admission", student.admittedOn?.toString() ?: "-"))
        }

        val closing = Paragraph(
            "Please confirm acceptance of this offer through your guardian portal. " +
                    "If you have any questions, kindly contact the Admissions Office. " +
                    "Congratulations once again on your successful admission."
        )

        content.add(title, sub, intro, details, closing)
    }

    private fun spanUnderline(text: String): Span =
        Span(text).apply {
            addClassNames("underline", "typewriter")
        }

    private fun detailParagraph(label: String, value: String): Paragraph =
        Paragraph().apply {
            add(Text("$label: "))
            add(spanUnderline(value))
        }
}