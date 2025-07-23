package com.sms.ui.guardian

import com.sms.entities.Guardian
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
        readOnlyFields = setOf("guardianId", "email", "phoneNumber") // Admin-initiated fields
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
        launchUiCoroutine {
            val guardian = getCurrentGuardian()
            ui.withUi {
                if (guardian != null) {
                    form.setGuardian(guardian)
                } else {
                    Notification.show("Error loading your profile", 3000, Notification.Position.MIDDLE)
                }
            }
        }
    }

    private suspend fun getCurrentGuardian(): Guardian? {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? User
        return user?.person as? Guardian
    }
}