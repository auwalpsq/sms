package com.sms.ui.admin.views

import com.sms.entities.SchoolClass
import com.sms.services.SchoolClassService
import com.sms.ui.admin.components.SchoolClassDialog
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.Menu
import jakarta.annotation.security.RolesAllowed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@PageTitle("School Classes")
@Route(value = "classes", layout = AdminView::class)
@RolesAllowed("ADMIN")
@Menu(order = 2.0, icon = "vaadin:group", title = "Manage Classes")
class SchoolClassView(
    private val schoolClassService: SchoolClassService
) : VerticalLayout() {

    private val grid = Grid(SchoolClass::class.java, false)
    private lateinit var dialog: SchoolClassDialog
    private val ui: UI = UI.getCurrent()

    init {
        setSizeFull()
        spacing = "false"
        isPadding = false

        configureGrid()
        configureDialog()

        val addButton = Button("Add Class", Icon(VaadinIcon.PLUS)).apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addClickListener { dialog.open(null) }
        }

        add(HorizontalLayout(addButton), grid)
        refreshGrid()
    }

    private fun configureGrid() {
        grid.addColumn(SchoolClass::name).setHeader("Class Name").setAutoWidth(true)
        grid.addColumn { it.section?.name ?: "" }.setHeader("Section").setAutoWidth(true)
        grid.addColumn { it.level?.name ?: "" }.setHeader("Level").setAutoWidth(true)
        grid.addColumn { it.grade?.name ?: "" }.setHeader("Grade").setAutoWidth(true)
        grid.addColumn(SchoolClass::classTeacher).setHeader("Class Teacher").setAutoWidth(true)

        grid.asSingleSelect().addValueChangeListener {
            it.value?.let { schoolClass -> dialog.open(schoolClass) }
        }
        grid.columns.forEach { column -> column.isAutoWidth = true }
    }

    private fun configureDialog() {
        dialog = SchoolClassDialog(
            emptyList(),
            onSave = {schoolClass -> schoolClassService.save(schoolClass)},
            onDelete = {schoolClass -> schoolClassService.deleteById(schoolClass.id)},
            onChange = { refreshGrid() }
        ).apply {
            configureDialogAppearance()
            width = "25%"
            maxWidth = "400px"
        }
    }

    private fun refreshGrid() {
        launchUiCoroutine {
            val classes = schoolClassService.findAll()
            ui?.withUi {
                grid.setItems(classes)
            }
        }
    }
}