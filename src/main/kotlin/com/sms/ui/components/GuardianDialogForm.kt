package com.sms.ui.components

import com.sms.entities.Guardian
import com.sms.ui.common.BaseFormDialog
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.notification.Notification
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

    private val phoneNumber = TextField("Phone Number").apply {
        isRequired = true
        setRequiredIndicatorVisible(true)
        setPlaceholder("08012345678")
    }

    override fun buildForm(formLayout: FormLayout) {
        formLayout.apply{
            responsiveSteps = listOf(FormLayout.ResponsiveStep("0", 1))
        }
        formLayout.add(firstName, lastName, email, phoneNumber)
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
        binder.forField(phoneNumber)
            .withValidator({ value ->
                value != null && value.matches(Regex("^0\\d{10}$"))
            }, "Phone number must be exactly 11 digits and start with 0")
            .bind(Guardian::phoneNumber) { g, v -> g.phoneNumber = v }
    }
    override fun showDeleteConfirmation() {
        val dialog = ConfirmDialog().apply {
            setHeader("Delete Guardian")
            setText("Are you sure you want to delete ${currentEntity.firstName} ${currentEntity.lastName}?")
            setCancelText("Cancel")
            setConfirmText("Delete")
            val ui : UI? = UI.getCurrent()
            addConfirmListener {
                launchUiCoroutine {
                    onDelete(currentEntity)
                    ui?.withUi {
                        close()
                        Notification.show(
                            "Guardian deleted successfully",
                            3000,
                            Notification.Position.TOP_CENTER
                        )
                    }
                }
            }
        }
        dialog.open()
    }
    init{
        configureDialogAppearance()
    }
    override fun createNewInstance(): Guardian = Guardian()
    override fun getEntityType(): Class<Guardian> = Guardian::class.java
}