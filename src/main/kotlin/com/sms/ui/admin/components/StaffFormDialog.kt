package com.sms.ui.admin.components

import com.sms.entities.*
import com.sms.enums.Gender
import com.sms.enums.Qualification
import com.sms.enums.StaffType
import com.sms.ui.common.BaseFormDialog
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.data.binder.ValidationResult

class StaffFormDialog(
    title: String,
    private val onSaveCallback: suspend (Staff) -> Unit,
    private val onDeleteCallback: suspend (Staff) -> Unit,
    private val onChangeCallback: () -> Unit,
    var onEmailCheck: (suspend  (String) -> Boolean)? = null
) : BaseFormDialog<Staff>(title, onSaveCallback, onDeleteCallback, onChangeCallback) {

    private val firstName = TextField("First Name")
    private val middleName = TextField("Middle Name")
    private val lastName = TextField("Last Name")
    private val gender = ComboBox<Gender>("Gender").apply { setItems(*Gender.values()) }
    private val email = EmailField("Email")
    private val phone = TextField("Phone Number")
    private val staffType = ComboBox<StaffType>("Staff Type").apply { setItems(*StaffType.values()) }
    private val qualification = ComboBox<Qualification>("Qualification").apply { setItems(*Qualification.values()) }
    private val specialization = TextField("Specialization")
    private val employmentDate = DatePicker("Employment Date")

    override fun buildForm(formLayout: FormLayout) {
        formLayout.add(
            firstName, middleName, lastName, gender,
            email, phone,
            staffType, qualification,
            specialization, employmentDate
        )
        formLayout.setResponsiveSteps(
            FormLayout.ResponsiveStep("0", 2),
            //FormLayout.ResponsiveStep("600px", 3)
        )
    }

    override fun configureBinder() {
        binder.forField(firstName).asRequired("First name required").bind(Staff::firstName.name)
        binder.forField(lastName).asRequired("Last name required").bind(Staff::lastName.name)
        binder.forField(email)
            .asRequired("Email required")
            .withValidator({ value ->
                // Basic email format validation
                value.contains("@") && value.contains(".")
            }, "Invalid email format")
            .withValidator { value, _ ->
                // This validator runs asynchronously to check duplicates
                launchUiCoroutine {
                    val exists = onEmailCheck?.invoke(value.trim().lowercase()) ?: false
                    ui?.withUi {
                        if (exists) {
                            email.isInvalid = true
                            email.errorMessage = "This email is already in use by another staff."
                        } else {
                            email.isInvalid = false
                            email.errorMessage = ""
                        }
                    }
                }
                // Always return true here since the UI will reflect invalid state manually
                ValidationResult.ok()
            }
            .bind(Staff::email.name)

        binder.forField(phone).asRequired("Phone required").bind(Staff::phoneNumber.name)
        binder.bindInstanceFields(this)
    }

    override fun createNewInstance(): Staff = Staff()

    override fun getEntityType(): Class<Staff> = Staff::class.java
}