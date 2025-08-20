package com.sms.ui.guardian.views

import com.sms.entities.Applicant
import com.sms.entities.Guardian
import com.sms.entities.User
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.services.SchoolClassService
import com.sms.ui.components.ApplicationFormDialog
import com.sms.ui.guardian.GuardianLayout
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.JavaScript
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@JavaScript("https://js.paystack.co/v1/inline.js")
@PageTitle("My Applications")
@Route(value = "guardian/admissions", layout = GuardianLayout::class)
@RolesAllowed("GUARDIAN")
@Menu(order = 2.0, icon = "vaadin:form", title = "Apply for Admission")
class GuardianApplicationView(
    private val applicantService: ApplicantService,
    private val guardianService: GuardianService,
    private val schoolClassService: SchoolClassService
) : VerticalLayout() {
    val user = SecurityContextHolder.getContext().authentication.principal as User
    private val grid = Grid(Applicant::class.java, false)
    private var formDialog: ApplicationFormDialog? = null
    private var currentGuardian: Guardian? = null
    val ui: UI? = UI.getCurrent()

    init {
        add(H2("Admission Applications"))

        launchUiCoroutine {
            val guardianId = user.person?.id
            if(guardianId != null){
                currentGuardian = guardianService.findById(guardianId)
                ui?.withUi{
                    createFormDialog()
                    setupLayout()
                    refreshGrid()
                }
            }
        }

        configureGrid()
    }

    private fun createFormDialog() {
        formDialog = ApplicationFormDialog(
            guardian = currentGuardian!!,
            schoolClassService = schoolClassService,
            onSave = { applicant ->
                applicantService.save(applicant)
            },
            onDelete = { applicant ->
                applicantService.delete(applicant.id!!)
            },
            onChange = { refreshGrid() }
        )
    }

    private fun setupLayout() {
        add(
            HorizontalLayout(
                Button("New Application", {
                    formDialog?.open(null)
                }).apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }
            ),
            grid
        )
    }

    private fun configureGrid() {
        grid.addColumn { it.applicationNumber }.setHeader("Application Number")
            .setFlexGrow(0)
            .isAutoWidth = true
        grid.addColumn { it.getFullName() }.setHeader("Full Name")
        grid.addColumn { it.gender }.setHeader("Gender")
        grid.addColumn { it.dateOfBirth }.setHeader("Date of Birth")
        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                Span(applicant.applicationStatus.name).apply {
                    element.setAttribute("theme", when (applicant.applicationStatus) {
                        Applicant.ApplicationStatus.PENDING -> "badge warning"
                        Applicant.ApplicationStatus.APPROVED -> "badge success"
                        Applicant.ApplicationStatus.REJECTED -> "badge error"
                    })
                }
            }
        ).setHeader("Status")

        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                HorizontalLayout().apply {
                    add(
                        Button(
                            if (applicant.applicationStatus == Applicant.ApplicationStatus.APPROVED)
                                "Complete" else "Edit"
                        ).apply {
                            addClickListener { formDialog?.open(applicant) }
                        },
                        Button("View Form").apply {
                            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                            addClickListener {
                                ui?.get()?.page?.open("/guardian/application-form/${applicant.id}", "_blank")
                            }
                        },
                        Button("Pay with Paystack", { e ->
                            UI.getCurrent().getPage().executeJs(
                                "var handler = PaystackPop.setup({" +
                                        "   key: 'pk_test_1c957236071be45d53fe766576b4f60aaaa0534c'," +  // your public key
                                        "   email: '${applicant.guardian?.email}'," +
                                        "   amount: 5000 * 100," +  // amount in kobo
                                        "   currency: 'NGN'," +
                                        "   callback: function(response) { $0.\$server.paymentSuccess(response.reference); }," +
                                        "   onClose: function() { alert('Payment window closed'); }" +
                                        "}); handler.openIframe();",
                                getElement()
                            )
                        })
                    )
                }
            }
        ).setHeader("Actions")

        //grid.setWidthFull()
        grid.columns.forEach { column -> column.isAutoWidth = true }
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
    }

    private fun refreshGrid() {
        currentGuardian?.id?.let { guardianId ->
            launchUiCoroutine {
                val applications = applicantService.findByGuardianId(guardianId)
                    .sortedByDescending { it.submissionDate }
                ui?.withUi { grid.setItems(applications) }
            }
        } ?: run {
            ui?.access { grid.setItems(emptyList()) }
        }
    }
}