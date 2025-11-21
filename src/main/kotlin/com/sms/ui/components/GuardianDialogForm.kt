package com.sms.ui.components

import com.sms.enums.Gender
import com.sms.entities.Guardian
import com.sms.enums.UserRole
import com.sms.services.ApplicantService
import com.sms.ui.common.BaseFormDialog
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.data.validator.StringLengthValidator
import kotlinx.coroutines.runBlocking

class GuardianDialogForm(
    private val applicantService: ApplicantService,
    private val adminMode: Boolean,
    private val isEmailTaken: suspend (String) -> Boolean,
    private val onAssignRoles: suspend (Guardian, Set<UserRole>) -> Unit = {_, _ ->},
    private val loadExistingRoles: suspend (Guardian) -> Set<UserRole> = {_ -> emptySet()},
    onSave: suspend (Guardian) -> Unit,
    onDelete: suspend (Guardian) -> Unit,
    onChange: () -> Unit
) : BaseFormDialog<Guardian>(
    dialogTitle = if (adminMode) "Add Guardian" else "Edit My Profile",
    onSave = onSave,
    onDelete = onDelete,
    onChange = onChange
) {

    // Person fields
    private val firstName = TextField("First Name").apply { isRequired = true }
    private val middleName = TextField("Middle Name")
    private val lastName = TextField("Last Name").apply { isRequired = true }
    private val gender = ComboBox<Gender>("Gender").apply {
        setItems(Gender.values().toList())
        isRequired = true
    }

    // ContactPerson fields
    private val email = EmailField("Email").apply { isRequired = true }
    private val phoneNumber = TextField("Phone Number").apply { isRequired = true }
    private val address = TextField("Address")
    private val city = TextField("City")
    private val state = TextField("State")

    // Guardian fields
    private val guardianId = TextField("Guardian ID")
    private val occupation = TextField("Occupation")
    private val employer = TextField("Employer")
    private val alternatePhone = TextField("Alternate Phone")

    private val roles = MultiSelectComboBox<UserRole>("Roles")

    override fun buildForm(formLayout: FormLayout) {

        if (adminMode) {
            // Admin only needs basic fields
            formLayout.responsiveSteps = listOf(FormLayout.ResponsiveStep("0", 1))
            formLayout.add(firstName, lastName, gender, email, phoneNumber, roles)

            roles.setItems(UserRole.GUARDIAN, UserRole.STAFF)

        } else {
            // Guardian sees all fields
            formLayout.add(
                firstName, middleName, lastName, gender,
                guardianId, email, phoneNumber,
                alternatePhone, occupation, employer,
                address, city, state
            )

            // Make some read-only
            firstName.isReadOnly = true
            lastName.isReadOnly = true
            guardianId.isReadOnly = true
            email.isReadOnly = true
            phoneNumber.isReadOnly = true
            gender.isReadOnly = true
        }
    }

    override fun configureBinder() {
        val nameValidator = StringLengthValidator("Must be at least 2 characters", 2, null)

        binder.forField(firstName)
            .withValidator(nameValidator)
            .bind(Guardian::firstName) { g, v -> g.firstName = v }

        binder.forField(middleName)
            .bind(Guardian::middleName, Guardian::middleName::set)

        binder.forField(lastName)
            .withValidator(nameValidator)
            .bind(Guardian::lastName) { g, v -> g.lastName = v }

        binder.forField(gender)
            .asRequired("Gender is required")
            .bind(Guardian::gender) { g, v -> g.gender = v }


        binder.forField(email)
            .withValidator(EmailValidator("Invalid email address"))
            .withValidator({ value ->
                runBlocking {
                    val trimmed = value?.trim().orEmpty()
                    val existing = currentEntity.email?.trim()
                    if (trimmed.equals(existing, ignoreCase = true)) true
                    else !isEmailTaken(trimmed)
                }
            }, "Email already registered")
            .bind(Guardian::email) { g, v -> g.email = v }

        binder.forField(phoneNumber)
            .withValidator({ value ->
                value != null && value.matches(Regex("^0\\d{10}$"))
            }, "Phone number must be exactly 11 digits and start with 0")
            .bind(Guardian::phoneNumber) { g, v -> g.phoneNumber = v }

        binder.forField(address).bind(Guardian::address) { g, v -> g.address = v }
        binder.forField(city).bind(Guardian::city) { g, v -> g.city = v }
        binder.forField(state).bind(Guardian::state) { g, v -> g.state = v }

        binder.forField(guardianId).bind(Guardian::guardianId) { g, v -> g.guardianId = v }
        binder.forField(occupation).bind(Guardian::occupation) { g, v -> g.occupation = v }
        binder.forField(employer).bind(Guardian::employer) { g, v -> g.employer = v }
        binder.forField(alternatePhone).bind(Guardian::alternatePhone) { g, v -> g.alternatePhone = v }
    }

    override fun showDeleteConfirmation(impactDescription: String?) {
        launchUiCoroutine {
            val dependents = applicantService.countByGuardian(currentEntity.id!!)
            val impact = if (dependents > 0) {
                "⚠️ This guardian has $dependents dependents linked. " +
                        "Deleting will also affect their applications."
            } else null

            ui?.withUi {
                super.showDeleteConfirmation(impactDescription = impact)
            }
        }
    }


    override fun onSaveClick() {
        if (binder.writeBeanIfValid(currentEntity)) {
            val selectedRoles = roles.value.toSet()
            launchUiCoroutine {
                try {
                    onSave(currentEntity)
                    onAssignRoles(currentEntity, selectedRoles)
                    ui?.withUi {
                        onChange()
                        close()
                        // Only show success in admin mode
                        if (adminMode) {
                            showSuccess("Saved successfully")
                        }
                    }
                } catch (ex: Exception) {
                    ui?.withUi { showError(ex.message.toString()) }
                }
            }
        }
    }
    override fun canDelete(entity: Guardian?): Boolean {
        // Guardians cannot delete themselves
        return adminMode && entity != null
    }


    init {
        configureDialogAppearance()
        width = if (adminMode) "25%" else "50%"
        maxWidth = "800px"
    }

    override fun populateForm(entity: Guardian?) {
        super.populateForm(entity)

        if (!adminMode) {
            roles.isVisible = false
            return
        }

        // Admin Mode → load roles for existing guardians
        val guardian = currentEntity

        if (guardian.id == 0L) {
            // Newly created guardian → default role
            roles.value = setOf(UserRole.GUARDIAN)
        } else {
            // Load saved roles using coroutine
            launchUiCoroutine {
                val assigned = loadExistingRoles(guardian)
                ui?.withUi {
                    roles.value = assigned
                }
            }
        }
    }

    override fun createNewInstance(): Guardian = Guardian()
    override fun getEntityType(): Class<Guardian> = Guardian::class.java
}