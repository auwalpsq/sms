package com.sms.ui.admin.components

import com.sms.entities.AcademicSession
import com.sms.enums.Term
import com.sms.ui.common.BaseFormDialog
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.data.binder.BeanValidationBinder

class AcademicSessionFormDialog(
    onSave: suspend (AcademicSession) -> Unit,
    onDelete: suspend (AcademicSession) -> Unit,
    onChange: () -> Unit
) : BaseFormDialog<AcademicSession>("Academic Session Form", onSave, onDelete, onChange) {

    private val yearField = IntegerField("Year").apply {
        min = 2000
        max = 2100
        isClearButtonVisible = true
    }

    private val termField = ComboBox<Term>("Term").apply {
        setItems(*Term.values())
        isClearButtonVisible = true
    }

    private val currentCheckbox = Checkbox("Current Session")

    override fun buildForm(formLayout: FormLayout) {
        formLayout.responsiveSteps = listOf(
            FormLayout.ResponsiveStep("0", 1)
        )
        formLayout.add(yearField, termField, currentCheckbox)
    }

    override fun configureBinder() {
        binder.forField(yearField).bind(AcademicSession::year, AcademicSession::year::set)
        binder.forField(termField).bind(AcademicSession::term, AcademicSession::term::set)
        binder.forField(currentCheckbox).bind(AcademicSession::isCurrent, AcademicSession::isCurrent::set)
    }

    override fun createNewInstance(): AcademicSession = AcademicSession()

    override fun getEntityType(): Class<AcademicSession> = AcademicSession::class.java
}