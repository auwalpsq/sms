package com.sms.ui.admin.views

import com.sms.entities.Guardian
import com.sms.services.ApplicantService
import com.sms.services.ContactPersonService
import com.sms.services.GuardianService
import com.sms.ui.components.GuardianDialogForm
import com.sms.ui.components.PaginationBar
import com.sms.ui.components.SearchBar
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class GuardiansView(
        private val guardianService: GuardianService,
        private val applicantService: ApplicantService,
        private val contactPersonService: ContactPersonService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private val grid = Grid(Guardian::class.java, false)
    private var dialog: GuardianDialogForm
    private var paginationBar: PaginationBar

    private var currentSearchQuery: String? = null

    init {
        setSizeFull()
        isSpacing = true

        // Initialize dialog
        dialog = createDialog()

        configureGrid()

        val searchBar =
                SearchBar("Search guardians...") { query ->
                    currentSearchQuery = query.trim().ifEmpty { null }
                    paginationBar.reset()
                    refreshGrid(1)
                }

        // ➕ Add button
        val addButton =
                Button("Add Guardian", Span().apply { addClassNames("ph", "ph-plus") }).apply {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    addClickListener { dialog.open(null) }
                }

        paginationBar = PaginationBar(pageSize = 10) { page -> refreshGrid(page) }

        val topBar =
                HorizontalLayout(addButton, searchBar).apply {
                    defaultVerticalComponentAlignment = FlexComponent.Alignment.END
                    width = "100%"
                    isSpacing = true
                    setPadding(false)
                    setMargin(false)
                }

        add(topBar, grid, paginationBar)
        paginationBar.reset()
    }

    private fun createDialog(): GuardianDialogForm {
        return GuardianDialogForm(
                applicantService = applicantService,
                adminMode = true,
                isEmailTaken = { email -> contactPersonService.emailExists(email) },
                onSave = { guardian ->
                    launchUiCoroutine {
                        guardianService.save(guardian)
                        ui.withUi { refreshGrid(paginationBar.getCurrentPage()) }
                    }
                },
                onDelete = { guardian ->
                    launchUiCoroutine {
                        guardianService.delete(guardian)
                        ui.withUi { refreshGrid(paginationBar.getCurrentPage()) }
                    }
                },
                onChange = { refreshGrid(paginationBar.getCurrentPage()) },
                onAssignRoles = { guardian, roles ->
                    launchUiCoroutine {
                        guardianService.updateGuardianRoles(guardian, roles) // <-- ADD THIS
                    }
                },
                loadExistingRoles = { guardian -> guardianService.getGuardianRoles(guardian) },
        )
    }

    private fun configureGrid() {
        grid.addColumn { it.getFullName() }.setHeader("Full Name").setAutoWidth(true)
        grid.addColumn { it.email }.setHeader("Email").setAutoWidth(true)
        grid.addColumn { it.phoneNumber }.setHeader("Phone Number").setAutoWidth(true)

        grid.addItemDoubleClickListener { event -> dialog.open(event.item) }
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        grid.isAllRowsVisible = true
    }

    private fun refreshGrid(page: Int = 1) {
        launchUiCoroutine {
            val pageSize = paginationBar.pageSize
            val result = guardianService.findPage(currentSearchQuery, page, pageSize)
            val totalCount = result.totalCount
            val guardians = result.items

            ui.withUi {
                grid.setItems(guardians)
                paginationBar.update(totalCount)
            }
        }
    }
}
