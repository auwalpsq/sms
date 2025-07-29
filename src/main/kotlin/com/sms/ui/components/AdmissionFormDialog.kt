package com.sms.ui.components

import com.sms.entities.Gender
import com.sms.entities.Student
import com.sms.ui.common.BaseFormDialog
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.validator.StringLengthValidator
import java.time.LocalDate

class AdmissionFormDialog(
    onSave: suspend (Student) -> Unit,
    onDelete: suspend (Student) -> Unit,
    onChange: () -> Unit
) : BaseFormDialog<Student>("Admission Application", onSave, onDelete, onChange) {

    private val firstName: TextField = TextField("First Name")
    private val lastName: TextField = TextField("Last Name")
    private val middleName: TextField = TextField("Middle Name")
    private val gender: ComboBox<Gender> = ComboBox<Gender>("Gender").apply {
        setItems(Gender.values().toList())
        isRequiredIndicatorVisible = true
    }
    private val dateOfBirth: DatePicker = DatePicker("Date of Birth").apply {
        isRequiredIndicatorVisible = true
        max = LocalDate.now().minusYears(1) // sanity check
    }

    override fun buildForm(formLayout: FormLayout) {
        formLayout.apply{
            responsiveSteps = listOf(FormLayout.ResponsiveStep("0", 1))
        }
        formLayout.add(firstName, lastName, middleName, gender, dateOfBirth)
    }

    override fun configureBinder() {
        binder.forField(firstName)
            .asRequired("First name is required")
            .withValidator(StringLengthValidator("Must be at least 2 characters", 2, 50))
            .bind(Student::firstName, Student::firstName::set)

        binder.forField(lastName)
            .asRequired("Last name is required")
            .withValidator(StringLengthValidator("Must be at least 2 characters", 2, 50))
            .bind(Student::lastName, Student::lastName::set)

        binder.forField(middleName)
            .withValidator(StringLengthValidator("Must be less than 50 characters", 0, 50))
            .bind(Student::middleName, Student::middleName::set)

        binder.forField(gender)
            .asRequired("Gender is required")
            .bind(Student::gender, Student::gender::set)

        binder.forField(dateOfBirth)
            .asRequired("Date of birth is required")
            .bind(Student::dateOfBirth, Student::dateOfBirth::set)
    }
    init{
        configureDialogAppearance()
    }
    override fun createNewInstance(): Student = Student()

    override fun getEntityType(): Class<Student> = Student::class.java
}