package com.sms.services

import com.princexml.wrapper.Prince
import com.princexml.wrapper.enums.InputType
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.ByteArrayOutputStream
import java.io.IOException

@Service
class ApplicationFormPdfService(
    private val templateEngine: TemplateEngine,
    private val prince: Prince
) {

    fun renderHtml(templateName: String, model: Map<String, Any>): String {
        val ctx = Context()
        ctx.setVariables(model)
        return templateEngine.process(templateName, ctx)
    }

    fun renderPdf(templateName: String, model: Map<String, Any>): ByteArray{
            val html = renderHtml(templateName, model)

            prince.setInputType(InputType.HTML)

            return ByteArrayOutputStream().use { out ->
                try {
                    val success = prince.convertString(html, out)
                    if (!success) throw IOException("Prince conversion failed")
                    out.toByteArray()
                } catch (ex: Exception) {
                    throw IOException("Prince PDF generation failed: ${ex.message}", ex)
                }
            }
        }
}