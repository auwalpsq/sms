package com.sms.controllers

import com.sms.services.PdfExportService
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicationPdfController(
    private val pdfExportService: PdfExportService,
    private val applicantService: ApplicantService,
    private val guardianService: GuardianService
) {

    @GetMapping("/applications/{id}/pdf")
    suspend fun downloadApplicationPdf(
        @PathVariable id: Long,
        response: HttpServletResponse
    ) {
        // 1. Load applicant & guardian from your services
        val applicant = applicantService.findById(id)
            ?: throw IllegalArgumentException("Applicant not found")
        val guardian = applicant.guardian
            ?: throw IllegalArgumentException("Guardian not found")

        // 2. Prepare Thymeleaf model
        val model = mapOf(
            "applicant" to applicant,
            "guardian" to guardian
        )

        // 3. Render PDF
        val pdfBytes = pdfExportService.renderPdf("application-form", model)

        // 4. Stream PDF back to browser
        response.contentType = "application/pdf"
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=application_${applicant.applicationNumber}.pdf"
        )
        response.outputStream.write(pdfBytes)
    }
}