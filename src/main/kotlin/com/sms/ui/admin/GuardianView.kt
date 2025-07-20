package com.sms.ui.admin

import com.sms.entities.Guardian
import com.sms.services.GuardianService
import com.sms.ui.components.GuardianDialogForm
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("admin/guardians", layout= AdminView::class)
@RolesAllowed("ADMIN")
class GuardianView(
    private val guardianService: GuardianService
) : VerticalLayout() {
    val ui = UI.getCurrent()

    private val grid = Grid(Guardian::class.java)
    private val dialog = GuardianDialogForm(
        isEmailTaken = { email -> guardianService.existsByEmail(email) },
        onSave = { guardian -> guardianService.save(guardian) },
        onDelete = { guardian -> guardianService.delete(guardian) },
        onChange = { refreshGrid() }
    )
    init {
        setSizeFull()
        spacing = "true"
        configureGrid()
        addToolbar()
        add(grid)
        refreshGrid()
    }

    private fun configureGrid() {
        grid.setSizeFull()
        grid.setColumns("firstName", "lastName", "email")
        grid.addItemDoubleClickListener { event ->
            dialog.open(event.item)
        }
    }

    private fun addToolbar() {
        val addButton = Button("Add Guardian") {
            dialog.open(null)
        }
        add(HorizontalLayout(addButton))
    }

    private fun refreshGrid() {
        launchUiCoroutine {
            val guardians = guardianService.findAll()
            ui.withUi {
                grid.setItems(guardians)
            }
        }
    }
}