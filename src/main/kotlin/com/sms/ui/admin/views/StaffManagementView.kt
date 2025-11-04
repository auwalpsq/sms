package com.sms.ui.admin.views

import com.sms.entities.Staff
import com.sms.services.StaffService
import com.sms.ui.admin.components.StaffFormDialog
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.ui.components.PaginationBar
import com.sms.ui.components.SearchBar
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@RolesAllowed("ADMIN")
@PageTitle("Manage Staff")
@Route(value = "admin/staff", layout = AdminView::class)
@Menu(order = 2.0, icon = "vaadin:users", title = "Manage Payments")
class StaffManagementView(
    private val staffService: StaffService
) : VerticalLayout() {

    private val ui: UI? = UI.getCurrent()
    private val grid = Grid(Staff::class.java, false)
    private val searchBar = SearchBar("Search by name, email or staff ID") { searchStaff() }
    private val pagination = PaginationBar(pageSize = 10) { loadPage(it) }

    private var currentQuery: String? = null

    init {
        setSizeFull()
        isSpacing = true
        isPadding = true

        configureGrid()
        configureLayout()

        loadPage(1)
    }

    private fun configureGrid() {
        grid.addColumn { it.getFullName() }.setHeader("Name").setAutoWidth(true).setFlexGrow(2)
        grid.addColumn { it.staffId }.setHeader("Staff ID").setAutoWidth(true)
        grid.addColumn { it.staffType }.setHeader("Type").setAutoWidth(true)
        grid.addColumn { it.qualification }.setHeader("Qualification").setAutoWidth(true)
        grid.addColumn { it.specialization ?: "-" }.setHeader("Specialization").setAutoWidth(true)
        grid.addColumn { it.phoneNumber }.setHeader("Phone").setAutoWidth(true)
        grid.addColumn { it.email }.setHeader("Email").setAutoWidth(true)

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        grid.isAllRowsVisible = false
        grid.setSizeFull()

        grid.addItemDoubleClickListener {
            openFormDialog(it.item)
        }
    }

    private fun configureLayout() {
        val addBtn = Button("Add Staff") {
            openFormDialog(Staff())
        }

        val header = HorizontalLayout(H3("Staff Management"), addBtn)
        header.setWidthFull()
        header.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        header.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER

        add(header, searchBar, grid, pagination)
    }

    private fun searchStaff() {
        currentQuery = searchBar.value
        pagination.reset()
    }

    private fun loadPage(page: Int) {
        launchUiCoroutine {
            val result = staffService.findAll(
                search = currentQuery,
                page = page - 1,
                size = pagination.pageSize
            )

            val total = staffService.countAll(currentQuery)

            ui?.withUi {
                grid.setItems(result)
                pagination.update(total)
            }
        }
    }

    private fun openFormDialog(staff: Staff) {
        val dialog = StaffFormDialog(
            title = if (staff.id == 0L) "Add New Staff" else "Edit Staff",
            onSaveCallback = { saved ->
                launchUiCoroutine {
                    if (saved.id == 0L) {
                        staffService.save(saved)
                        showSuccess("Staff added successfully.")
                    } else {
                        staffService.update(saved)
                        showError("Staff updated successfully.")
                    }
                    loadPage(pagination.getCurrentPage())
                }
            },
            onDeleteCallback = { toDelete ->
                launchUiCoroutine {
                    staffService.delete(toDelete.id)
                    Notification.show("Staff deleted successfully.")
                    loadPage(pagination.getCurrentPage())
                }
            },
            onChangeCallback = {
                // refresh grid or any dynamic UI updates if needed
                loadPage(pagination.getCurrentPage())
            }
        )

        // Preload data into the dialog for editing
        dialog.populateForm(staff)

        dialog.open()
    }
}