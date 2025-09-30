package com.sms.ui.admin.components

import com.sms.entities.Applicant
import com.sms.entities.SchoolClass
import com.sms.services.SchoolClassService
import com.sms.services.StudentService
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class AssignClassDialog(
    private val applicant: Applicant,
    private val schoolClassService: SchoolClassService,
    private val studentService: StudentService
) : Dialog() {

    private val ui: UI? = UI.getCurrent()

    private val classSelect = ComboBox<SchoolClass>("Select Class").apply {
        setItemLabelGenerator { it.name }
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

            launchUiCoroutine {
                try {
                    studentService.assignClass(applicant.id!!, selectedClass.id)
                    ui?.withUi {
                        showSuccess("Assigned ${applicant.getFullName()} to ${selectedClass.name}")
                        close()
                    }
                } catch (ex: Exception) {
                    ui?.withUi { showError("Failed to assign: ${ex.message}") }
                }
            }
        }

        val cancelButton = Button("Cancel") { close() }

        add(
            VerticalLayout(
                classSelect,
                assignButton,
                cancelButton
            )
        )

        // Load available classes
        launchUiCoroutine {
            val classes = schoolClassService.findAll()
            ui?.withUi { classSelect.setItems(classes) }
        }
    }
}
