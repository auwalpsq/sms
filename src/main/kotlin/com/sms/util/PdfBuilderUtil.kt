package com.sms.util

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.sms.entities.Applicant
import java.io.ByteArrayOutputStream

fun htmlToPdfBytes(html: String): ByteArray {
    val out = ByteArrayOutputStream()
    PdfRendererBuilder()
        .useFastMode()
        .withHtmlContent(html, null)
        .toStream(out)
        .run()
    return out.toByteArray()
}

fun buildPdfHtml(applicant: Applicant): String {
    // Use a very simple HTML snapshot of the form.
    return """
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="UTF-8">
            <style>
              body { font-family: Arial, sans-serif; font-size: 12px; }
              h1 { text-align: center; }
              table { width: 100%; border-collapse: collapse; margin: 12px 0; }
              th, td { border: 1px solid #ccc; padding: 6px; text-align: left; }
            </style>
          </head>
          <body>
            <h1>Application Form</h1>
            <h2>Personal Information</h2>
            <table>
              <tr><th>Full Name</th><td>${applicant.getFullName()}</td></tr>
              <tr><th>Gender</th><td>${applicant.gender}</td></tr>
              <tr><th>Date of Birth</th><td>${applicant.dateOfBirth}</td></tr>
              <tr><th>Age</th><td>${applicant.currentAge}</td></tr>
              <tr><th>Blood Group</th><td>${applicant.bloodGroup}</td></tr>
            </table>
            <h2>Application Information</h2>
            <table>
              <tr><th>Application Number</th><td>${applicant.applicationNumber ?: ""}</td></tr>
              <tr><th>Status</th><td>${applicant.applicationStatus}</td></tr>
              <tr><th>Payment</th><td>${applicant.paymentStatus}</td></tr>
            </table>
            <h2>Guardian</h2>
            <table>
              <tr><th>Full Name</th><td>${applicant.guardian?.getFullName() ?: ""}</td></tr>
              <tr><th>Email</th><td>${applicant.guardian?.email ?: ""}</td></tr>
              <tr><th>Phone</th><td>${applicant.guardian?.phoneNumber ?: ""}</td></tr>
            </table>
          </body>
        </html>
    """.trimIndent()
}