package com.sms.ui.guardian.views

import com.sms.entities.Applicant
import com.sms.entities.Guardian
import com.sms.entities.Payment
import com.sms.entities.User
import com.sms.enums.PaymentCategory
import com.sms.enums.PaymentStatus
import com.sms.services.AcademicSessionService
import com.sms.services.ApplicantService
import com.sms.services.GuardianService
import com.sms.services.PaymentService
import com.sms.services.PaymentTypeService
import com.sms.services.PaymentVerificationService
import com.sms.services.PaystackService
import com.sms.services.SchoolClassService
import com.sms.ui.common.showError
import com.sms.ui.common.showSuccess
import com.sms.ui.components.ApplicationFormDialog
import com.sms.ui.guardian.GuardianLayout
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.ClientCallable
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.JavaScript
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@JavaScript("https://js.paystack.co/v2/inline.js")
@PageTitle("My Applications")
@Route(value = "guardian/admissions", layout = GuardianLayout::class)
@RolesAllowed("GUARDIAN")
@Menu(order = 2.0, icon = "vaadin:form", title = "Apply for Admission")
class GuardianApplicationView(
    private val paymentVerificationService: PaymentVerificationService,
    private val applicantService: ApplicantService,
    private val guardianService: GuardianService,
    private val schoolClassService: SchoolClassService,
    private val paystackService: PaystackService,
    private val paymentTypeService: PaymentTypeService,
    private val academicSessionService: AcademicSessionService,
    private val paymentService: PaymentService
) : VerticalLayout() {
    val user = SecurityContextHolder.getContext().authentication.principal as User
    private val grid = Grid(Applicant::class.java, false)
    private var formDialog: ApplicationFormDialog? = null
    private var currentGuardian: Guardian? = null
    private val ui: UI? = UI.getCurrent()

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
                Span(applicant.paymentStatus.name).apply {
                    element.setAttribute("theme", when (applicant.paymentStatus) {
                        Applicant.PaymentStatus.UNPAID -> "badge error"
                        Applicant.PaymentStatus.PAID -> "badge success"
                        Applicant.PaymentStatus.PARTIALLY_PAID -> "badge contrast" // or "badge warning"
                    })
                }
            }
        ).setHeader("Payment")
        grid.addColumn(
            ComponentRenderer { applicant: Applicant ->
                MenuBar().apply {
                    val menu = addItem("Actions")
                        menu.subMenu.addItem("Edit", { formDialog?.open(applicant)})
                        menu.subMenu.addItem("View Form", { ui?.get()?.page?.open("/guardian/application-form/${applicant.id}", "_blank") })
                        menu.subMenu.addItem("Pay", { UI.getCurrent().page.executeJs(
                            """
                                        const paystack = new PaystackPop();
                                        paystack.newTransaction({
                                           key: 'pk_test_1c957236071be45d53fe766576b4f60aaaa0534c',
                                           email: '${applicant.guardian?.email}',
                                           amount: 5000 * 100,
                                           currency: 'NGN',
                                           onSuccess: (transaction) => { 
                                               $0.${'$'}server.paymentSuccess(transaction.reference); 
                                           },
                                           onClose: () => { alert('Payment window closed'); }
                                        });
                                        """.trimIndent(),
                            this@GuardianApplicationView)
                        })
                    menu.subMenu.addItem("Test Payment", {
                        launchUiCoroutine {
                            val paymentType = paymentTypeService.findByCategory(PaymentCategory.APPLICATION)
                            val session = academicSessionService.findCurrent()

                            // Check if payment already exists
                            var payment = paymentService.findByApplicantAndTypeAndSessionAndTerm(
                                applicant.id!!, paymentType?.id!!, session?.id!!, session.term
                            )

                            if (payment != null) {
                                if (payment.status == PaymentStatus.SUCCESS) {
                                    UI.getCurrent().withUi { showSuccess("✅ You have already paid for this application.") }
                                    return@launchUiCoroutine
                                }
                                // Reuse existing reference if pending
                            } else {
                                // Create new payment
                                val reference = UUID.randomUUID().toString()
                                payment = Payment(
                                    applicant = applicant,
                                    guardian = applicant.guardian,
                                    paymentType = paymentType,
                                    academicSession = session,
                                    term = session.term,
                                    reference = reference,
                                    status = PaymentStatus.PENDING
                                )

                                paymentService.save(payment)
                            }

                            // Launch Paystack with existing/new reference
                            val reference = payment.reference
                            ui?.get()?.withUi {
                                ui?.get()?.page?.executeJs(
                                    """
                                const paystack = new PaystackPop();
                                paystack.newTransaction({
                                   key: 'pk_test_1c957236071be45d53fe766576b4f60aaaa0534c',
                                   email: '${applicant.guardian?.email}',
                                   amount: ${paymentType.amount} * 100,
                                   currency: 'NGN',
                                   reference: '$reference',
                                   onSuccess: (transaction) => { 
                                       $0.${'$'}server.paymentSuccess(transaction.reference);
                                   },
                                   onClose: () => { alert('Payment window closed'); }
                                });
                                """.trimIndent(),
                                    this@GuardianApplicationView
                                )
                            }
                        }

                    })
                }
            }
        ).setHeader("More Actions")

        grid.setWidthFull()
        //grid.columns.forEach { column -> column.isAutoWidth = true }
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
    @ClientCallable
    fun paymentSuccess(reference: String) {
        launchUiCoroutine {
            val verified = paystackService.verify(reference)
            if(verified){
                paymentVerificationService.verifyAndUpdateApplicant(reference)
            }
            ui?.withUi {
                if (verified) {
                    showSuccess("✅ Payment verified and recorded. Ref: $reference")
                } else {
                    showError("❌ Payment verification failed. Please try again.")
                }
                refreshGrid()
            }
        }
    }
}