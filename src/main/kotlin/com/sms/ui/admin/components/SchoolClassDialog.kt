package com.sms.ui.admin.components

import com.sms.entities.SchoolClass
import com.sms.entities.Staff
import com.sms.enums.Grade
import com.sms.enums.Level
import com.sms.enums.Section
import com.sms.ui.common.BaseFormDialog
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.Span

class SchoolClassDialog(
    private val teachers: List<Staff>?,
    onSave: suspend (SchoolClass) -> Unit,
    onDelete: suspend (SchoolClass) -> Unit,
    onChange: () -> Unit
) : BaseFormDialog<SchoolClass>(
    dialogTitle = "Manage Class",
    onSave = onSave,
    onDelete = onDelete,
    onChange = onChange
) {
    private lateinit var sectionField: ComboBox<Section>
    private lateinit var levelField: ComboBox<Level>
    private lateinit var gradeField: ComboBox<Grade>
    private lateinit var namePreview: Span

    override fun buildForm(formLayout: FormLayout) {
        sectionField = ComboBox<Section>("Section").apply {
            setItems(*Section.values())
        }
        levelField = ComboBox<Level>("Level").apply {
            setItems(*Level.values())
        }
        gradeField = ComboBox<Grade>("Grade").apply {
            setItems(*Grade.values())
        }

        namePreview = Span().apply {
            element.style["fontStyle"] = "italic"
            element.style["color"] = "var(--lumo-secondary-text-color)"
        }

        // Generate name dynamically
        val updateName: () -> Unit = {
            val section = sectionField.value
            val level = levelField.value
            val grade = gradeField.value
            namePreview.text = if (section != null && level != null && grade != null) {
                "${section} ${level.number}${grade}"
            } else {
                ""
            }
        }
        sectionField.addValueChangeListener { updateName() }
        levelField.addValueChangeListener { updateName() }
        gradeField.addValueChangeListener { updateName() }

        formLayout.add(sectionField, levelField, gradeField, namePreview)
        formLayout.responsiveSteps = listOf(
            FormLayout.ResponsiveStep("0", 1)
        )
    }

    override fun configureBinder() {
        binder.forField(sectionField)
            .asRequired("Section is required")
            .bind(SchoolClass::section, SchoolClass::section::set)

        binder.forField(levelField)
            .asRequired("Level is required")
            .bind(SchoolClass::level, SchoolClass::level::set)

        binder.forField(gradeField)
            .asRequired("Grade is required")
            .bind(SchoolClass::grade, SchoolClass::grade::set)

    }

    override fun createNewInstance(): SchoolClass = SchoolClass()
    override fun getEntityType(): Class<SchoolClass> = SchoolClass::class.java
}