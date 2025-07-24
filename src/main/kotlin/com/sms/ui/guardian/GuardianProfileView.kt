package com.sms.ui.guardian

import com.sms.entities.User
import com.sms.services.GuardianService
import com.sms.ui.components.GuardianProfileForm
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@Route("guardian/profile", layout = GuardianLayout::class)
@RolesAllowed("GUARDIAN")
@PageTitle("My Profile")
class GuardianProfileView(
    private val guardianService: GuardianService
) : VerticalLayout() {

    private val ui = UI.getCurrent()
    private val form = GuardianProfileForm(
        readOnlyFields = setOf("firstName", "middleName", "lastName", "guardianId", "email", "phoneNumber") // Admin-initiated fields
    )

    init {
        setSizeFull()
        spacing = "true"
        isPadding = true

        // Configure form actions
        form.addSaveListener { guardian ->
            launchUiCoroutine {
                guardianService.save(guardian)
                ui.withUi {
                    Notification.show("Profile updated successfully", 3000, Notification.Position.TOP_CENTER)
                }
            }
        }

        form.addCancelListener {
            // Reload original data
            loadGuardianData()
        }

        add(form)

        // Load guardian data
        loadGuardianData()
    }

    private fun loadGuardianData() {
        val guardianId = getCurrentGuardianId()
        launchUiCoroutine {
            val guardian = guardianService.findById(guardianId)
            ui.withUi {
                if (guardian != null) {
                    form.setGuardian(guardian)
                } else {
                    Notification.show("Error loading your profile", 3000, Notification.Position.MIDDLE)
                }
            }
        }
    }

    private fun getCurrentGuardianId(): Long? {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? User
        val guardianId = user?.person?.id
        return guardianId
    }
}