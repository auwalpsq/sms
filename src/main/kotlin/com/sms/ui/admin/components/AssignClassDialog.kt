package com.sms.ui.admin.components

import com.sms.entities.SchoolClass
import com.sms.enums.Section
import com.sms.services.SchoolClassService
import com.sms.ui.common.showError
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class AssignClassDialog(
    private val schoolClassService: SchoolClassService,
    private val onAssigned: (SchoolClass) -> Unit
) : Dialog() {

    private val ui: UI? = UI.getCurrent()

    // 🔹 First combo: pick section (Nursery, Primary, Secondary…)
    private val sectionSelect = ComboBox<Section>("Select Section").apply {
        setItems(*Section.values())
        setItemLabelGenerator { it.name }
    }

    // 🔹 Second combo: classes for that section
    private val classSelect = ComboBox<SchoolClass>("Select Class").apply {
        setItemLabelGenerator { it.name }
        isEnabled = false
    }

    init {
        width = "400px"
        isModal = true
        isDraggable = true
        isResizable = false

        val assignButton = Button("Assign") {
            val selectedClass = classSelect.value
            if (selectedClass == null) {
                showError("Please select a class")
                return@Button
            }
            onAssigned(selectedClass)
            close()
        }

        val cancelButton = Button("Cancel") { close() }

        val buttons = HorizontalLayout(assignButton, cancelButton).apply {
            justifyContentMode = FlexComponent.JustifyContentMode.END
            width = "100%"
        }

        add(
            VerticalLayout(
                sectionSelect,
                classSelect,
                buttons
            )
        )

        // 🔹 React to section change
        sectionSelect.addValueChangeListener { event ->
            val selectedSection = event.value
            if (selectedSection != null) {
                classSelect.isEnabled = true
                launchUiCoroutine {
                    val classes = schoolClassService.findBySection(selectedSection)
                    ui?.withUi { classSelect.setItems(classes) }
                }
            } else {
                classSelect.clear()
                classSelect.isEnabled = false
            }
        }
    }
}