package com.sms.ui.components

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Paragraph

class SchoolHeader(
    private val schoolName: String = "SCHOOL NAME PLACEHOLDER",
    private val address: String = "Address Line 1, City, State",
    private val contactInfo: String = "Phone: +234 XXX XXX XXXX | Email: info@school.com",
    private val logoPath: String = "images/placeholder.png"
) : Div() {

    init {
        addClassName("application-letterhead")
        style["text-align"] = "center"

        val logo = Image(logoPath, "School Logo").apply {
            addClassName("logo")
            style["max-height"] = "80px"
            style["margin-bottom"] = "0.5rem"
        }

        add(
            logo,
            H1(schoolName),
            Paragraph(address),
            Paragraph(contactInfo)
        )
    }
}