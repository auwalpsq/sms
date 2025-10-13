package com.sms.services

import com.princexml.wrapper.Prince
import com.princexml.wrapper.enums.InputType
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

@Service
class ApplicationFormPdfService(
    private val templateEngine: TemplateEngine,
    private val prince: Prince
) {
    private val cssFiles = listOf(
        "/static/css/school-header.css",
        "/static/css/application-form.css",
        "/static/css/admission-letter.css"
    )

    private val combinedCss: String by lazy {
        cssFiles.joinToString("\n") { path ->
            val cssStream = this::class.java.getResourceAsStream(path)
                ?: throw IOException("Could not load CSS file at $path")
            cssStream.readBytes().toString(StandardCharsets.UTF_8)
        }
    }


    fun renderHtml(templateName: String, model: Map<String, Any?>): String {
        val ctx = Context()
        ctx.setVariables(model)

        // Render template HTML
        val html = templateEngine.process(templateName, ctx)

        // Inject CSS before </head>
        return html.replaceFirst(
            "</head>",
            "<style>$combinedCss</style></head>"
        )
    }

    fun renderPdf(templateName: String, model: Map<String, Any?>): ByteArray{
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