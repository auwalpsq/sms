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
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.theme.lumo.LumoUtility

abstract class BaseFormDialog<T : Any>(
    private val dialogTitle: String,
    private val onSave: suspend (T) -> Unit,
    private val onDelete: suspend (T) -> Unit,
    private val onChange: () -> Unit
) : Dialog() {
    val ui: UI? = UI.getCurrent()

    protected val binder: Binder<T> = Binder(getEntityType())
    protected lateinit var currentEntity: T

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
        setDraggable(true)
        setResizable(true)
        configureButtonActions()
    }
    private fun configureButtonActions() {
        saveBtn.addClickListener {
            if (binder.writeBeanIfValid(currentEntity)) {
                launchUiCoroutine {
                    onSave(currentEntity)
                    ui?.withUi {
                        onChange()
                        close()
                        Notification.show("Saved successfully", 3000, Notification.Position.TOP_CENTER)
                    }
                }
            }
        }

        deleteBtn.addClickListener {
            launchUiCoroutine {
                onDelete(currentEntity)
                ui?.withUi {
                    onChange()
                    close()
                    Notification.show("Deleted successfully", 3000, Notification.Position.TOP_CENTER)
                }
            }
        }

        cancelBtn.addClickListener {
            close()
        }
    }
    fun configureDialogAppearance() {
        // Header
        val header = H3(dialogTitle).apply {
            addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE)
        }

        // Form layout
        val formLayout = FormLayout().apply {
            responsiveSteps = listOf(
                FormLayout.ResponsiveStep("0", 1),
                FormLayout.ResponsiveStep("500px", 2)
            )
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
        deleteBtn.isVisible = entity != null
        binder.readBean(currentEntity)
        open()
    }
    protected open fun showDeleteConfirmation() {
        val dialog = ConfirmDialog().apply {
            setHeader("Confirm Delete")
            setText("Are you sure you want to delete this item?")
            setCancelText("Cancel")
            setConfirmText("Delete")
            addConfirmListener {
                launchUiCoroutine {
                    onDelete(currentEntity)
                    ui?.withUi {
                        onChange()
                        close()
                        Notification.show(
                            "Deleted successfully",
                            3000,
                            Notification.Position.TOP_CENTER
                        )
                    }
                }
            }
        }
        dialog.open()
    }
    protected abstract fun buildForm(formLayout: FormLayout)
    protected abstract fun configureBinder()
    protected abstract fun createNewInstance(): T
    protected abstract fun getEntityType(): Class<T>
}