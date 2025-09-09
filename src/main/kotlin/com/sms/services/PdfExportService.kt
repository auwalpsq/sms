package com.sms.services

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.ByteArrayOutputStream

@Service
class PdfExportService(
    private val templateEngine: TemplateEngine
) {

    /**
     * Render Thymeleaf template into raw HTML string
     */
    fun renderHtml(templateName: String, model: Map<String, Any>): String {
        val context = Context()
        context.setVariables(model)
        return templateEngine.process(templateName, context)
    }

    /**
     * Render Thymeleaf template into PDF bytes
     */
    fun renderPdf(templateName: String, model: Map<String, Any>): ByteArray {
        val html = renderHtml(templateName, model)

        val out = ByteArrayOutputStream()
        PdfRendererBuilder().useFastMode()
            .withHtmlContent(html, null) // second param is base URI (null = classpath)
            .toStream(out)
            .run()

        return out.toByteArray()
    }
}