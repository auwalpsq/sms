package com.sms.ui.admin

import com.sms.entities.Guardian
import com.sms.services.GuardianService
import com.sms.ui.components.GuardianDialogForm
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("admin/guardians", layout = AdminView::class)
@RolesAllowed("ADMIN")
@PageTitle("Guardian Management")
@Menu(order = 0.0, icon = "vaadin:user", title = "Gurdians")
class GuardianView(
    private val guardianService: GuardianService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private val grid = Grid(Guardian::class.java)
    private lateinit var dialog: GuardianDialogForm

    init {
        setSizeFull()
        spacing = "true"

        // Initialize dialog first
        dialog = createDialog()

        configureGrid()
        addToolbar()
        add(grid)
        refreshGrid()
    }

    private fun createDialog(): GuardianDialogForm {
        return GuardianDialogForm(
            isEmailTaken = { email -> guardianService.existsByEmail(email) },
            onSave = { guardian ->
                guardianService.save(guardian)
                refreshGrid()
            },
            onDelete = { guardian ->
                guardianService.delete(guardian)
                refreshGrid()
            },
            onChange = { refreshGrid() }
        )
    }

    private fun configureGrid() {
        grid.setSizeFull()
        grid.setColumns("firstName", "middleName", "lastName", "email", "phoneNumber")
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