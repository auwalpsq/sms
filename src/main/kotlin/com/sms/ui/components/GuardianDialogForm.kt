package com.sms.ui.components

import com.sms.entities.Guardian
import com.sms.ui.common.BaseFormDialog
import com.sms.util.launchUiCoroutine
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
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
) : BaseFormDialog<Guardian>(
    dialogTitle = "Guardian",
    onSave = onSave,
    onDelete = onDelete,
    onChange = onChange
) {

    private val firstName = TextField("First Name").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
    }

    private val lastName = TextField("Last Name").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
    }

    private val email = EmailField("Email").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
    }

    override fun buildForm(formLayout: FormLayout) {
        formLayout.add(firstName)
        formLayout.add(lastName)
        formLayout.add(email)

        // Configure responsive behavior
        formLayout.setColspan(email, 2)
    }

    override fun configureBinder() {
        val nameValidator = StringLengthValidator("Must be at least 2 characters", 2, null)

        binder.forField(firstName)
            .withValidator(nameValidator)
            .bind(Guardian::firstName) { g, v -> g.firstName = v }

        binder.forField(lastName)
            .withValidator(nameValidator)
            .bind(Guardian::lastName) { g, v -> g.lastName = v }

        binder.forField(email)
            .withValidator(EmailValidator("Invalid email address"))
            .withValidator( { value ->
                runBlocking {
                    val trimmed = value?.trim().orEmpty()
                    val existing = currentEntity.email?.trim()
                    if (trimmed.equals(existing, ignoreCase = true)) true
                    else !isEmailTaken(trimmed)
                }
            }, "Email already registered").bind(Guardian::email) { g, v -> g.email = v }
    }
    private fun showDeleteConfirmation(
        guardian: Guardian,
        onDelete: suspend (Guardian) -> Unit
    ) {
        
    }
    init{
        configureDialogAppearance()
    }
    override fun createNewInstance(): Guardian = Guardian()
    override fun getEntityType(): Class<Guardian> = Guardian::class.java
}