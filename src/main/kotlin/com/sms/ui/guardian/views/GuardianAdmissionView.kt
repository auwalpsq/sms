package com.sms.ui.guardian.views

import com.sms.entities.Guardian
import com.sms.entities.Student
import com.sms.entities.User
import com.sms.services.StudentService
import com.sms.ui.components.AdmissionFormDialog
import com.sms.ui.guardian.GuardianLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("My Applications")
@Route(value = "guardian/admissions", layout = GuardianLayout::class)
@RolesAllowed("GUARDIAN")
class GuardianAdmissionView(
    private val studentService:
    StudentService
) : VerticalLayout() {

    private val grid = Grid(Student::class.java, false)
    private val formDialog: AdmissionFormDialog

    init {
        add(H2("Admission Applications"))

        formDialog = AdmissionFormDialog(
            onSave = { student ->
                val user = SecurityContextHolder.getContext().authentication.principal as User
                student.guardian = user?.person as? Guardian
                student.applicationStatus = Student.ApplicationStatus.PENDING
                studentService.save(student)
            },
            onDelete = { student ->
                studentService.delete(student.id!!)
            },
            onChange = { refreshGrid() }
        )

        configureGrid()
        add(
            HorizontalLayout(
                Button("New Application", {
                    formDialog.open(null)
                }).apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }
            ),
            grid
        )

        refreshGrid()
    }

    private fun configureGrid() {
        grid.addColumn { it.firstName }.setHeader("First Name")
        grid.addColumn { it.lastName }.setHeader("Last Name")
        grid.addColumn { it.gender }.setHeader("Gender")
        grid.addColumn { it.dateOfBirth }.setHeader("Date of Birth")
        grid.addColumn { it.applicationStatus }.setHeader("Status")

        grid.addColumn(
            ComponentRenderer { student: Student ->
                Button("Edit").apply {
                    isEnabled = student.applicationStatus == Student.ApplicationStatus.PENDING
                    addClickListener { formDialog.open(student) }
                }
            }
        ).setHeader("Actions")
        grid.setWidthFull()
    }

    private fun refreshGrid() {
        val user = SecurityContextHolder.getContext().authentication.principal as User
        val guardian = user?.person as? Guardian
        guardian?.id?.let { guardianId ->
            val applications = studentService.findByGuardianId(guardianId)
            grid.setItems(applications)
        } ?: run {
            grid.setItems(emptyList())
        }
    }
}