package com.sms.ui.common

import com.sms.entities.Student
import com.sms.services.ApplicationFormPdfService
import com.sms.services.StudentService
import com.sms.ui.components.PhotoUploadField
import com.sms.ui.components.SchoolHeader
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.Html
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
import java.nio.file.Path

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
    private val passportField = PhotoUploadField(Path.of("src/main/resources/static/images/passports"))

    init {
        addClassName("admission-letter")
        setSizeFull()
        isPadding = true
        isSpacing = true
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER

        content.addClassName("admission-content")

        // --- Buttons ---
        val printBtn = Button("Print", VaadinIcon.PRINT.create()).apply {
            addClickListener { ui?.get()?.page?.executeJs("window.print();") }
        }
        val imageUrl = this::class.java.getResource("/static/images/passports/placeholder.png")?.toExternalForm()
            ?: throw IllegalStateException("Image not found in resources")

        val downloadBtn = Anchor(
            DownloadHandler.fromInputStream { _ ->
                val passport = this::class.java.getResource("/static/images/passports/${student?.applicant?.photoUrl}")?.toExternalForm()
                    ?: throw IllegalStateException("Passport not found in resources")
                val s = student ?: return@fromInputStream null
                val model = mapOf(
                    "student" to s,
                    "applicant" to s.applicant,
                    "guardian" to s.applicant.guardian,
                    "schoolClass" to s.admittedClass,
                    "session" to s.admittedSession,
                    "schoolLogo" to imageUrl,
                    "passport" to passport

                )
                val pdfBytes = applicationFormPdfService.renderPdf("admission-letter", model)
                DownloadResponse(
                    ByteArrayInputStream(pdfBytes),
                    "admission-letter-${s.admissionNumber}.pdf",
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

        add(content, btnBar)
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

        // --- School header ---
        val header = SchoolHeader()

        // --- Passport photo setup (view-only) ---
        passportField.apply {
            setPhotoUrl(applicant.photoUrl ?: "/images/placeholder.png")
            upload.isVisible = false
            replaceButton.isVisible = false
            imagePreview.isVisible = true
            imagePreview.width = "80px"
            imagePreview.height = "80px"
            content.alignItems = FlexComponent.Alignment.END
        }

        // --- Layout: Header on top, passport below (right aligned) ---
        val headerContainer = VerticalLayout().apply {
            width = "100%"
            isPadding = false
            isSpacing = false
            alignItems = FlexComponent.Alignment.CENTER
            add(header)

            val passportWrapper = HorizontalLayout(passportField).apply {
                width = "100%"
                justifyContentMode = FlexComponent.JustifyContentMode.END
                style["margin-top"] = "8px"
            }

            add(passportWrapper)
        }

        val title = H1("Admission Letter")
        val sub = H2("Admission Offer for ${applicant.getFullName()}")

        val intro = Paragraph().apply {
            add(Text("Dear "))
            add(spanUnderline(guardian?.getFullName() ?: "Parent/Guardian"))
            add(Text(","))
            add(Text(" We are delighted to inform you that "))
            add(spanUnderline(applicant.getFullName()))
            add(Text(" has been offered admission into "))
            add(spanUnderline(schoolClass.name))
            add(Text(" for the "))
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

        content.add(headerContainer, title, sub, intro, details, closing)
    }

    private fun spanUnderline(text: String): Span =
        Span(text).apply {
            addClassNames("underline")
            style.set("border-bottom", "1px solid #000")
            style.set("padding-bottom", "2px")
            style.set("font-weight", "600")
        }

    private fun detailParagraph(label: String, value: String): Paragraph =
        Paragraph().apply {
            add(Html("<strong>$label:</strong> "), spanUnderline(value))
        }
}