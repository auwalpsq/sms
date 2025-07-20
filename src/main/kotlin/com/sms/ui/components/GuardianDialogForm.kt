package com.sms.ui.components

import com.sms.entities.Guardian
import com.sms.ui.common.BaseFormDialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.data.validator.StringLengthValidator
import kotlinx.coroutines.runBlocking

class GuardianDialogForm(
    private val isEmailTaken: suspend (String) -> Boolean,
    onSave: suspend (Guardian) -> Unit,
    onDelete: suspend (Guardian) -> Unit,
    onChange: () -> Unit
) : BaseFormDialog<Guardian>(onSave, onDelete, onChange) {

    private val firstName = TextField("First Name")
    private val lastName = TextField("Last Name")
    private val email = EmailField("Email")

    override fun buildForm(formLayout: FormLayout) {
        formLayout.add(firstName, lastName, email)
    }

    override fun configureBinder() {
        val nameValidator = StringLengthValidator("Must be at least 2 characters", 2, null)

        binder.forField(firstName)
            .asRequired("First name is required")
            .withValidator(nameValidator)
            .bind(Guardian::firstName) { g, v -> g.firstName = v }

        binder.forField(lastName)
            .asRequired("Last name is required")
            .withValidator(nameValidator)
            .bind(Guardian::lastName) { g, v -> g.lastName = v }

        binder.forField(email)
            .asRequired("Email is required")
            .withValidator(EmailValidator("Invalid email"))
            .withValidator( { value ->
                runBlocking {
                    val trimmed = value?.trim().orEmpty()
                    val existing = currentEntity.email?.trim()
                    if (trimmed.equals(existing, ignoreCase = true)) true
                    else !isEmailTaken(trimmed)
                }
            }, "Email already registered")
            .bind(Guardian::email) { g, v -> g.email = v }
    }
    init {
        myInit()
    }
    override fun createNewInstance(): Guardian = Guardian()
    override fun getEntityType(): Class<Guardian> = Guardian::class.java
}