package com.sms.ui.admin.views

import com.sms.ui.admin.components.AcademicSessionFormDialog
import com.sms.entities.AcademicSession
import com.sms.services.AcademicSessionService
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

@Route("admin/sessions", layout = AdminView::class)
@PageTitle("Manage Academic Sessions")
@RolesAllowed("ADMIN")
@Menu(order = 0.0, icon = "vaadin:calendar", title = "Manage Academic Sessions")
class ManageAcademicSessionsView(
    private val sessionService: AcademicSessionService
) : VerticalLayout() {

    private val grid = Grid(AcademicSession::class.java, false)
    private lateinit var formDialog: AcademicSessionFormDialog
    private val ui: UI? = UI.getCurrent()

    init {
        configureGrid()
        configureDialog()
        add(buildToolbar(), grid)
        refreshGrid()
    }

    private fun configureGrid() {
        grid.addColumn { it.displaySession }.setHeader("Session")
        grid.addColumn { it.term }.setHeader("Term")
        grid.addColumn { if (it.isCurrent) "Yes" else "No" }.setHeader("Current?")
        grid.asSingleSelect().addValueChangeListener {
            it.value?.let { session -> formDialog.open(session) }
        }
    }

    private fun configureDialog() {
        formDialog = AcademicSessionFormDialog(
            onSave = { academicSession -> sessionService.save(academicSession) },
            onDelete = { academicSession -> sessionService.deleteById(academicSession.id) },
            onChange = { refreshGrid() }
        ).apply {
            configureDialogAppearance()
            maxWidth = "400px"
            width = "25%"
        }
    }

    private fun buildToolbar(): HorizontalLayout {
        val addBtn = Button("Add Academic Session") {
            formDialog.open(null)
        }
        return HorizontalLayout(addBtn)
    }

    private fun refreshGrid() {
        launchUiCoroutine {
            val sessions = sessionService.findAll()
            ui?.withUi {
                grid.setItems(sessions ?: emptyList())
            }
        }
    }
}