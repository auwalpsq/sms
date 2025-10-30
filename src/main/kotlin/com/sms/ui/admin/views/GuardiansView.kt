package com.sms.ui.admin.views

import com.sms.entities.Guardian
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.ui.components.GuardianDialogForm
import com.sms.ui.components.SearchBar
import com.sms.ui.components.PaginationBar
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class GuardiansView(
    private val guardianService: GuardianService,
    private val applicantService: ApplicantService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private val grid = Grid(Guardian::class.java, false)
    private lateinit var dialog: GuardianDialogForm
    private lateinit var paginationBar: PaginationBar

    init {
        setSizeFull()
        isSpacing = true

        // Initialize dialog
        dialog = createDialog()

        configureGrid()

        // 🔍 Search bar
        val searchBar = SearchBar("Search guardians...") { query ->
            launchUiCoroutine {
                val guardians = if (query.isBlank()) {
                    guardianService.findPage(paginationBar.getCurrentPage(), 3)
                } else {
                    guardianService.search(query)
                }
                ui?.withUi {
                    paginationBar.update(guardians.size)
                    grid.setItems(guardians)
                }
            }
        }

        // ➕ Add button
        val addButton = Button("Add Guardian", Icon(VaadinIcon.PLUS)).apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addClickListener { dialog.open(null) }
        }

        // 🧭 Pagination
        paginationBar = PaginationBar(pageSize = 3) { page ->
            refreshGrid(page)
        }

        val topBar = HorizontalLayout(addButton, searchBar).apply {
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
            isEmailTaken = { email -> guardianService.existsByEmail(email) },
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
            onChange = { refreshGrid(paginationBar.getCurrentPage()) }
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
            val guardians = guardianService.findPage(page, 3)
            ui.withUi {
                paginationBar.update(guardians.size)
                grid.setItems(guardians)
                grid.recalculateColumnWidths()
            }
        }
    }
}