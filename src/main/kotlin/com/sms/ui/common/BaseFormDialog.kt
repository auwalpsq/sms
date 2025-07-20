package com.sms.ui.common

import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.binder.Binder

abstract class BaseFormDialog<T : Any>(
    private val onSave: suspend (T) -> Unit,
    private val onDelete: suspend (T) -> Unit,
    private val onChange: () -> Unit
) : Dialog() {
    val ui = UI.getCurrent()

    protected val binder: Binder<T> = Binder(getEntityType())
    protected lateinit var currentEntity: T

    protected val saveBtn = Button("Save")
    protected val deleteBtn = Button("Delete")
    protected val cancelBtn = Button("Cancel")

    init {
        isCloseOnEsc = true
        isCloseOnOutsideClick = false
    }

    fun open(entity: T?) {
        currentEntity = entity ?: createNewInstance()
        deleteBtn.isVisible = entity != null
        binder.readBean(currentEntity)
        open()
    }
    fun myInit(){
        val formLayout = FormLayout()
        buildForm(formLayout)

        val actions = HorizontalLayout(saveBtn, deleteBtn, cancelBtn)
        val content = VerticalLayout(formLayout, actions)
        add(content)

        configureBinder()
        configureActions()
    }
    protected abstract fun buildForm(formLayout: FormLayout)
    protected abstract fun configureBinder()
    protected abstract fun createNewInstance(): T
    protected abstract fun getEntityType(): Class<T>

    private fun configureActions() {
        saveBtn.addClickListener {
            if (binder.writeBeanIfValid(currentEntity)) {
                launchUiCoroutine {
                    onSave(currentEntity)
                    ui.withUi {
                        onChange()
                        close()
                        Notification.show("Saved", 3000, Notification.Position.TOP_CENTER)
                    }
                }
            }
        }

        deleteBtn.addClickListener {
            launchUiCoroutine {
                onDelete(currentEntity)
                ui.withUi {
                    onChange()
                    close()
                    Notification.show("Deleted", 3000, Notification.Position.TOP_CENTER)
                }
            }
        }

        cancelBtn.addClickListener { close() }
    }
}