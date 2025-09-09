package com.sms.controllers

import com.lowagie.text.Document
import com.lowagie.text.pdf.PdfWriter
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import jakarta.servlet.http.HttpServletResponse
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.context.Context
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

@Controller
class ApplicationFormController(
    private val templateEngine: SpringTemplateEngine
) {

    // Show HTML view
    @GetMapping("/application-form/view")
    fun showForm(model: Model): String {
        val applicant = mapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "dateOfBirth" to "2010-01-01",
            "guardianName" to "Jane Doe"
        )
        model.addAttribute("applicant", applicant)
        return "application-form"
    }

    // Export PDF
    @GetMapping("/application-form/pdf")
    fun exportPdf(response: HttpServletResponse) {
        val applicant = mapOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "dateOfBirth" to "2010-01-01",
            "guardianName" to "Jane Doe"
        )

        val context = Context().apply { setVariable("applicant", applicant) }
        val htmlContent = templateEngine.process("application-form", context)

        // Convert HTML to PDF using OpenPDF
        val baos = ByteArrayOutputStream()
        val document = Document()
        PdfWriter.getInstance(document, baos)
        document.open()
        com.lowagie.text.html.simpleparser.HTMLWorker(document)
            .parse(ByteArrayInputStream(htmlContent.toByteArray(StandardCharsets.UTF_8)).reader())
        document.close()

        // Return PDF response
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "attachment; filename=application-form.pdf")
        response.outputStream.write(baos.toByteArray())
    }
}