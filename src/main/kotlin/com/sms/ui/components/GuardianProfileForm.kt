package com.sms.ui.components

import com.sms.entities.Guardian
import com.sms.entities.RelationshipType
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder

class GuardianProfileForm(
    private val readOnlyFields: Set<String> = setOf("guardianId")
) : VerticalLayout() {

    val binder = Binder<Guardian>(Guardian::class.java)
    var currentGuardian: Guardian? = null
        private set

    // Person fields
    private val firstName = TextField("First Name")
    private val middleName = TextField("Middle Name")
    private val lastName = TextField("Last Name")

    // ContactPerson fields
    private val phoneNumber = TextField("Phone Number")
    private val email = EmailField("Email")
    private val address = TextField("Address")
    private val city = TextField("City")
    private val state = TextField("State")

    // Guardian fields
    private val guardianId = TextField("Guardian ID")
    private val relationship = ComboBox<RelationshipType>("Relationship to Student")
    private val occupation = TextField("Occupation")
    private val employer = TextField("Employer")
    private val alternatePhone = TextField("Alternate Phone")

    // Buttons
    private val saveButton = Button("Save")
    private val cancelButton = Button("Cancel")

    init {
        setSizeFull()
        spacing = "true"
        isPadding = true

        // Configure form layout
        val formLayout = FormLayout().apply {
            responsiveSteps = listOf(
                FormLayout.ResponsiveStep("0", 1),
                FormLayout.ResponsiveStep("500px", 2)
            )
        }

        // Add fields to form
        with(formLayout) {
            addFormItem(firstName, "First Name")
            addFormItem(middleName, "Middle Name")
            addFormItem(lastName, "Last Name")
            addFormItem(phoneNumber, "Phone Number")
            addFormItem(email, "Email")
            addFormItem(address, "Address")
            addFormItem(city, "City")
            addFormItem(state, "State")
            addFormItem(guardianId, "Guardian ID")
            addFormItem(relationship, "Relationship")
            addFormItem(occupation, "Occupation")
            addFormItem(employer, "Employer")
            addFormItem(alternatePhone, "Alternate Phone")
        }

        // Configure relationship combo box
        relationship.setItems(RelationshipType.values().toList())

        // Configure binder
        configureBinder()

        // Button layout
        val buttonLayout = HorizontalLayout(saveButton, cancelButton).apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.END
            spacing = "true"
        }

        // Add components to layout
        add(H3("Guardian Profile"), formLayout, buttonLayout)
    }

    private fun configureBinder() {
        // Person fields
        binder.forField(firstName)
            .asRequired("First name is required")
            .bind(Guardian::firstName, Guardian::firstName::set)

        binder.forField(lastName)
            .asRequired("Last name is required")
            .bind(Guardian::lastName, Guardian::lastName::set)

        binder.forField(middleName)
            .bind(Guardian::middleName, Guardian::middleName::set)

        // ContactPerson fields
        binder.forField(phoneNumber)
            .asRequired("Phone number is required")
            .bind(Guardian::phoneNumber, Guardian::phoneNumber::set)

        binder.forField(email)
            .asRequired("Email is required")
            .bind(Guardian::email, Guardian::email::set)

        binder.forField(address)
            .bind(Guardian::address, Guardian::address::set)

        binder.forField(city)
            .bind(Guardian::city, Guardian::city::set)

        binder.forField(state)
            .bind(Guardian::state, Guardian::state::set)

        // Guardian fields
        binder.forField(guardianId)
            .bind({ it.guardianId }, null) // Read-only binding

        binder.forField(relationship)
            .asRequired("Relationship is required")
            .bind(Guardian::relationshipToStudent, Guardian::relationshipToStudent::set)

        binder.forField(occupation)
            .bind(Guardian::occupation, Guardian::occupation::set)

        binder.forField(employer)
            .bind(Guardian::employer, Guardian::employer::set)

        binder.forField(alternatePhone)
            .bind(Guardian::alternatePhone, Guardian::alternatePhone::set)
    }

    fun setGuardian(guardian: Guardian) {
        currentGuardian = guardian
        binder.readBean(guardian)
        setFieldsReadOnly()
    }

    private fun setFieldsReadOnly() {
        readOnlyFields.forEach { fieldName ->
            when (fieldName) {
                "guardianId" -> guardianId.isReadOnly = true
                "email" -> email.isReadOnly = true
                "phoneNumber" -> phoneNumber.isReadOnly = true
                // Add other fields as needed
            }
        }
    }

    fun addSaveListener(listener: (Guardian) -> Unit) {
        saveButton.addClickListener {
            if (binder.writeBeanIfValid(currentGuardian)) {
                currentGuardian?.let { listener(it) }
            }
        }
    }

    fun addCancelListener(listener: () -> Unit) {
        cancelButton.addClickListener { listener() }
    }
}