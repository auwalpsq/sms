package com.sms.ui.components

import com.sms.entities.Applicant
import com.sms.entities.Gender
import com.sms.entities.Guardian
import com.sms.entities.RelationshipType
import com.sms.entities.SchoolClass
import com.sms.enums.Section
import com.sms.services.SchoolClassService
import com.sms.ui.common.BaseFormDialog
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.validator.StringLengthValidator
import java.time.LocalDate

class ApplicationFormDialog(
    private val guardian: Guardian,
    private val schoolClassService: SchoolClassService,
    onSave: suspend (Applicant) -> Unit,
    onDelete: suspend (Applicant) -> Unit,
    onChange: () -> Unit
) : BaseFormDialog<Applicant>("Admission Application", onSave, onDelete, onChange) {

    private val uii: UI? = UI.getCurrent()
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
    val relationship = ComboBox<RelationshipType>("Relationship to Guardian").apply {
        //setItems(*RelationshipType.values())
        setItemLabelGenerator { it.name.lowercase().replaceFirstChar { ch -> ch.uppercase() } }
    }
    val previousSchoolName = TextField("Previous School")
    val previousClass = TextField("Previous Class")

    val applicationSection = ComboBox<Section>("Section").apply {
        setItems(Section.values().toList())
        isRequiredIndicatorVisible = true
        setItemLabelGenerator { it.name.lowercase().replaceFirstChar(Char::uppercase) }
        addValueChangeListener { event ->
            val selectedSection = event.value
            intendedClass.clear() // removes previous items & value
            if (selectedSection != null) {
                launchUiCoroutine {
                    val classes = schoolClassService.findBySection(selectedSection)
                    uii?.withUi {
                        intendedClass.setItems(classes)
                        intendedClass.value = classes.firstOrNull() // avoids exception
                    }
                }
            }
        }
    }

    val intendedClass = ComboBox<String>("Intended Class").apply {
        isRequiredIndicatorVisible = true
        setItems(emptyList())
        //setItemLabelGenerator{it.toString()}
    }
    override fun buildForm(formLayout: FormLayout) {
        formLayout.apply{
            responsiveSteps = listOf(
                FormLayout.ResponsiveStep("0", 1),
                FormLayout.ResponsiveStep("500px", 2)
            )
        }
        formLayout.add(firstName, lastName, middleName, gender, dateOfBirth,
            relationship, previousSchoolName, previousClass, applicationSection, intendedClass)
    }

    override fun configureBinder() {
        binder.forField(firstName)
            .asRequired("First name is required")
            .withValidator(StringLengthValidator("Must be at least 2 characters", 2, 50))
            .bind(Applicant::firstName, Applicant::firstName::set)

        binder.forField(lastName)
            .asRequired("Last name is required")
            .withValidator(StringLengthValidator("Must be at least 2 characters", 2, 50))
            .bind(Applicant::lastName, Applicant::lastName::set)

        binder.forField(middleName)
            .withValidator(StringLengthValidator("Must be less than 50 characters", 0, 50))
            .bind(Applicant::middleName, Applicant::middleName::set)

        binder.forField(gender)
            .asRequired("Gender is required")
            .bind(Applicant::gender, Applicant::gender::set)

        binder.forField(dateOfBirth)
            .asRequired("Date of birth is required")
            .bind(Applicant::dateOfBirth, Applicant::dateOfBirth::set)

        binder.forField(relationship)
            .asRequired("Relationship is required")
            .bind(Applicant::relationshipToGuardian, Applicant::relationshipToGuardian::set)

        relationship.setItems(RelationshipType.values().toSet())
        //relationship.value = currentEntity?.relationshipToStudent

        binder.forField(previousSchoolName)
            .bind(Applicant::previousSchoolName, Applicant::previousSchoolName::set)

        binder.forField(previousClass)
            .bind(Applicant::previousClass, Applicant::previousClass::set)

        binder.forField(applicationSection)
            .asRequired("Section is required")
            .bind(Applicant::applicationSection, Applicant::applicationSection::set)

        binder.forField(intendedClass)
            .asRequired("Intended class is required")
            .bind(Applicant::intendedClass, Applicant::intendedClass::set)
    }
    init{
        configureDialogAppearance()
    }
    override fun populateForm(entity: Applicant) {
        super.populateForm(entity)
        relationship.value = entity.relationshipToGuardian

        if (entity.applicationSection != null) {
            launchUiCoroutine {
                val classes = schoolClassService.findBySection(entity.applicationSection)
                uii?.withUi {
                    intendedClass.setItems(classes)
                    if (classes.contains(entity.intendedClass)) {
                        intendedClass.value = entity.intendedClass
                    }
                }
            }
        }
    }
    override fun createNewInstance(): Applicant = Applicant(guardian = guardian)

    override fun getEntityType(): Class<Applicant> = Applicant::class.java
}