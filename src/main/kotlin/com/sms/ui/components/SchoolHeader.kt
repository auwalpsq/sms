package com.sms.ui.components

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Paragraph

class SchoolHeader(
    private val schoolName: String = "SCHOOL NAME PLACEHOLDER",
    private val address: String = "Address Line 1, City, State",
    private val contactInfo: String = "Phone: +234 XXX XXX XXXX | Email: info@school.com",
    private val logoPath: String = "images/passports/placeholder.png"
) : Div() {

    init {
        addClassName("school-header")

        val logo = Image(logoPath, "School Logo").apply {
            addClassName("school-header-logo")
        }

        val title = H1(schoolName).apply {
            addClassName("school-header-title")
        }

        val addressParagraph = Paragraph(address).apply {
            addClassName("school-header-address")
        }

        val contactParagraph = Paragraph(contactInfo).apply {
            addClassName("school-header-contact")
        }

        add(logo, title, addressParagraph, contactParagraph)
    }
}
