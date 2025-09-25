package com.sms.ui.common

import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.theme.lumo.LumoUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseFormDialog<T : Any>(
    protected val dialogTitle: String,
    protected val onSave: suspend (T) -> Unit,
    protected val onDelete: suspend (T) -> Unit,
    protected val onChange: () -> Unit
) : Dialog() {
    protected open val ui: UI? = UI.getCurrent()

    protected val binder: Binder<T> = Binder(getEntityType())
    protected lateinit var currentEntity: T

    protected lateinit var formLayout: FormLayout

    protected val saveBtn = Button("Save").apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        isEnabled = false
        element.style.set("cursor", "not-allowed")
        setTooltipText("Complete all required fields to enable save")
    }

    protected val deleteBtn = Button("Delete").apply {
        addThemeVariants(ButtonVariant.LUMO_ERROR)
        isVisible = false
    }

    protected val cancelBtn = Button("Cancel").apply {
        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        addClickListener { close() }
    }

    init {
        // Dialog behavior
        isCloseOnEsc = true
        isCloseOnOutsideClick = false
        width = "50%"
        minWidth = "400px"
        setDraggable(true)
        setResizable(true)
        configureButtonActions()
    }
    protected open fun onSaveClick() {
        if (binder.writeBeanIfValid(currentEntity)) {
            launchUiCoroutine {
                try {
                    onSave(currentEntity)
                    ui?.withUi {
                        onChange()
                        close()
                        showSuccess("Saved successfully")
                    }
                } catch (ex: Exception) {
                    ui?.withUi { showError(ex.message.toString()) }
                }
            }
        }
    }

    private fun configureButtonActions() {
        saveBtn.addClickListener { onSaveClick() }
        deleteBtn.addClickListener { showDeleteConfirmation(); close() }
        cancelBtn.addClickListener { close() }
    }
    fun configureDialogAppearance() {
        // Header
        val header = H3(dialogTitle).apply {
            addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE)
        }

        // Form layout
        formLayout = FormLayout().apply {
            buildForm(this)
        }

        // Button bar
        val buttonLayout = HorizontalLayout(saveBtn, deleteBtn, cancelBtn).apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.END
            spacing = "true"
        }

        // Main content
        val content = VerticalLayout(header, formLayout, buttonLayout).apply {
            setSizeFull()
            isPadding = false
            spacing = "false"
        }

        add(content)
        configureBinder()
        configureValidityListener()
    }

    private fun configureValidityListener() {
        binder.addStatusChangeListener { event ->
            saveBtn.isEnabled = event.binder.isValid
            saveBtn.element.style.set(
                "cursor",
                if (event.binder.isValid) "pointer" else "not-allowed"
            )
        }
    }

    fun open(entity: T?) {
        currentEntity = entity ?: createNewInstance()
        // use hook instead of forcing delete
        deleteBtn.isVisible = canDelete(entity)
        binder.readBean(currentEntity)
        populateForm(entity)
        open()
    }

    /**
     * Hook: override in subclasses if deletion should be restricted.
     * By default, allows delete when entity is not null.
     */
    protected open fun canDelete(entity: T?): Boolean = entity != null

    protected open fun showDeleteConfirmation(impactDescription: String? = null) {
        val dialog = ConfirmDialog().apply {
            setHeader("Confirm Delete")

            val baseText = "Are you sure you want to delete this item?"
            setText(
                if (impactDescription.isNullOrBlank()) baseText
                else "$baseText\n\n$impactDescription"
            )

            setCancelable(true)
            setCancelText("Cancel")

            setConfirmText("Delete")
            val ui: UI = UI.getCurrent()
            addConfirmListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        onDelete(currentEntity)
                        ui?.withUi {
                            onChange()
                            close()
                            showSuccess("Deleted successfully")
                        }
                    } catch (ex: Exception) {
                        ui?.withUi { showError("Failed to delete: ${ex.message}") }
                    }
                }
            }
        }
        dialog.open()
    }

    open fun populateForm(entity: T?) {
        binder.readBean(entity)
    }
    protected abstract fun buildForm(formLayout: FormLayout)
    protected abstract fun configureBinder()
    protected abstract fun createNewInstance(): T
    protected abstract fun getEntityType(): Class<T>
}