package com.sms.ui.admin.views

import com.sms.entities.Guardian
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.ui.components.GuardianDialogForm
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class GuardiansView(
    private val guardianService: GuardianService,
    private val applicantService: ApplicantService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private val grid = Grid(Guardian::class.java, false)
    private lateinit var dialog: GuardianDialogForm

    init {
        setSizeFull()
        isSpacing = true

        // Initialize dialog first
        dialog = createDialog()

        configureGrid()
        addToolbar()
        add(grid)
        refreshGrid()
    }

    private fun createDialog(): GuardianDialogForm {
        return GuardianDialogForm(
            applicantService = applicantService,
            adminMode = true, // ✅ admin adds/edit minimal guardian fields
            isEmailTaken = { email -> guardianService.existsByEmail(email) },
            onSave = { guardian ->
                launchUiCoroutine {
                    guardianService.save(guardian)
                    ui.withUi { refreshGrid() }
                }
            },
            onDelete = { guardian ->
                launchUiCoroutine {
                    guardianService.delete(guardian)
                    ui.withUi { refreshGrid() }
                }
            },
            onChange = { refreshGrid() }
        )
    }

    private fun configureGrid() {
        grid.addColumn { it.getFullName() }.setHeader("Full Name")
        grid.addColumn { it.email }.setHeader("Email")
        grid.addColumn { it.phoneNumber }.setHeader("Phone Number")

        grid.addItemDoubleClickListener { event ->
            dialog.open(event.item)
        }

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
    }

    private fun addToolbar() {
        val addButton = Button("Add Guardian") {
            dialog.open(null) // opens in admin mode with empty Guardian
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