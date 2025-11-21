package com.sms.ui.admin.components

import com.sms.entities.Staff
import com.sms.enums.Gender
import com.sms.enums.Qualification
import com.sms.enums.StaffType
import com.sms.ui.common.BaseFormDialog
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField

class StaffFormDialog(
    title: String,
    onSaveCallback: suspend (Staff) -> Unit,
    onDeleteCallback: suspend (Staff) -> Unit,
    onChangeCallback: () -> Unit,
    private val onEmailCheck: suspend (String) -> Boolean,
    private val onPhoneCheck: suspend (String) -> Boolean
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

    // Tracks the async email check result
    private var emailValid = false

    private var phoneValid = false

    override fun buildForm(formLayout: FormLayout) {
        formLayout.add(
            firstName, middleName,
            lastName, gender,
            email, phone, staffType,
            qualification, specialization,
            employmentDate
        )

        formLayout.setResponsiveSteps(
            FormLayout.ResponsiveStep("0", 2)
        )

        setupEmailValidation()
        setupPhoneValidation()
    }

    override fun configureBinder() {
        binder.forField(firstName)
            .asRequired("First name required")
            .bind(Staff::firstName.name)

        binder.forField(middleName)
            .asRequired("Middle name required")
            .bind(Staff::middleName.name)

        binder.forField(lastName)
            .asRequired("Last name required")
            .bind(Staff::lastName.name)

        binder.forField(gender)
            .asRequired("Gender required")
            .bind(Staff::gender.name)

        binder.forField(email)
            .asRequired("Email required")
            .bind(Staff::email.name)

        binder.forField(phone)
            .asRequired("Phone number required")
            .bind(Staff::phoneNumber.name)

        binder.forField(staffType)
            .asRequired("Staff type required")
            .bind(Staff::staffType.name)

        binder.forField(qualification)
            .asRequired("Qualification required")
            .bind(Staff::qualification.name)

        binder.forField(specialization)
            .asRequired("Specialization required")
            .bind(Staff::specialization.name)

        binder.forField(employmentDate)
            .asRequired("Employment date required")
            .bind(Staff::employmentDate.name)

        // ensure binder tracks all fields
        binder.bindInstanceFields(this)
    }

    private fun setupEmailValidation() {
        // Clear previous helper text
        email.helperText = ""

        email.addValueChangeListener { event ->
            val value = event.value?.trim().orEmpty()

            // Reset state
            emailValid = false
            email.isInvalid = false
            email.errorMessage = null
            email.helperText = ""

            // Required check
            if (value.isEmpty()) {
                email.isInvalid = true
                email.errorMessage = "Email required"
                return@addValueChangeListener
            }

            // Format check
            if (!value.contains("@") || !value.contains(".")) {
                email.isInvalid = true
                email.errorMessage = "Invalid email format"
                return@addValueChangeListener
            }

            // Show "Checking..." while async verification happens
            email.helperText = "Checking..."
            email.element.style.set("color", "var(--lumo-secondary-text-color)")

            launchUiCoroutine {
                val exists = try {
                    onEmailCheck(value)
                } catch (ex: Exception) {
                    ui?.withUi {
                        email.isInvalid = true
                        email.errorMessage = "Unable to verify email"
                        email.helperText = ""
                    }
                    return@launchUiCoroutine
                }

                ui?.withUi {
                    val existing = currentEntity.email?.trim()
                    if (exists && !value.equals(existing, ignoreCase = true)) {
                        email.isInvalid = true
                        email.errorMessage = "Email already registered"
                        email.helperText = ""
                        emailValid = false
                    } else {
                        email.isInvalid = false
                        email.errorMessage = null
                        email.helperText = "✅ Available"
                        email.element.style.set("color", "var(--lumo-success-color)")
                        emailValid = true
                    }
                }
            }
        }
    }

    private fun setupPhoneValidation() {
        // Clear any default helper text
        phone.helperText = ""

        phone.addValueChangeListener { event ->
            val value = event.value?.trim().orEmpty()

            // Reset state
            phoneValid = false
            phone.isInvalid = false
            phone.errorMessage = null
            phone.helperText = ""
            phone.element.style.remove("color")

            // Required check
            if (value.isEmpty()) {
                phone.isInvalid = true
                phone.errorMessage = "Phone number required"
                return@addValueChangeListener
            }

            // Basic Nigeria-friendly digit check
            if (!value.matches(Regex("^\\d{11,15}\$"))) {
                phone.isInvalid = true
                phone.errorMessage = "Invalid phone number format"
                return@addValueChangeListener
            }

            // While async validation is happening:
            phone.helperText = "Checking..."
            phone.element.style.set("color", "var(--lumo-secondary-text-color)")

            launchUiCoroutine {
                val exists = try {
                    onPhoneCheck(value)
                } catch (ex: Exception) {
                    ui?.withUi {
                        phone.isInvalid = true
                        phone.errorMessage = "Unable to verify phone number"
                        phone.helperText = ""
                    }
                    return@launchUiCoroutine
                }

                ui?.withUi {
                    val existing = currentEntity.phoneNumber?.trim()

                    if (exists && !value.equals(existing, ignoreCase = true)) {
                        phone.isInvalid = true
                        phone.errorMessage = "Phone number already registered"
                        phone.helperText = ""
                        phoneValid = false
                    } else {
                        phone.isInvalid = false
                        phone.errorMessage = null
                        phone.helperText = "✅ Available"
                        phone.element.style.set("color", "var(--lumo-success-color)")
                        phoneValid = true
                    }
                }
            }
        }
    }

    override fun onSaveClick() {
        if (binder.writeBeanIfValid(currentEntity) && emailValid) {
            launchUiCoroutine {
                try {
                    onSave(currentEntity)
                    ui?.withUi {
                        onChange()
                        close()
                        showSuccess("Staff added successfully")
                    }
                } catch (ex: Exception) {
                    ui?.withUi { showError(ex.message.toString()) }
                }
            }
        }
    }
    override fun configureValidityListener() {
        binder.addStatusChangeListener { event ->
            saveBtn.isEnabled = event.binder.isValid && emailValid && phoneValid
            saveBtn.element.style.set(
                "cursor",
                if (event.binder.isValid) "pointer" else "not-allowed"
            )
        }
    }


    override fun createNewInstance(): Staff = Staff()

    override fun getEntityType(): Class<Staff> = Staff::class.java
}