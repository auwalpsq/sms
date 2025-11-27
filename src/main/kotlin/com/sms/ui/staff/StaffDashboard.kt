package com.sms.ui.staff

import com.sms.broadcast.UiBroadcaster
import com.sms.services.AcademicSessionService
import com.sms.ui.common.showInteractiveNotification
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.avatar.AvatarVariant
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.menubar.MenuBarVariant
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.server.menu.MenuConfiguration
import com.vaadin.flow.server.menu.MenuEntry
import com.vaadin.flow.theme.lumo.LumoUtility
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("Staff Portal")
@Route(value = "staff")
@RolesAllowed("STAFF")   // Admin can also enter staff dashboard
class StaffDashboard(
    private val sessionService: AcademicSessionService
) : AppLayout() {

    private val ui: UI = UI.getCurrent()

    init {
        createHeader()
        createDrawer()
        registerStaffNotifications()     // → Optional, same pattern as guardian
    }

    private fun createHeader() {
        val logo = H3("Staff Dashboard")

        val layout = HorizontalLayout().apply {
            setWidthFull()
            addClassNames(
                LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Padding.Horizontal.MEDIUM
            )
            add(logo)
        }

        // Load active academic session
        launchUiCoroutine {
            val session = sessionService.findCurrent()
            ui.withUi {
                val sessionInfo = session?.let {
                    Span("${it.displaySession} - ${it.term} Term")
                } ?: Span("No active academic session")

                sessionInfo.addClassNames(
                    LumoUtility.FontWeight.BOLD,
                    LumoUtility.FontSize.MEDIUM
                )
                layout.add(sessionInfo)
            }
        }

        addToNavbar(DrawerToggle(), layout)
    }

    private fun createDrawer() {
        addToDrawer(Scroller(createSideNav()), createUserMenu())
    }

    private fun createSideNav(): SideNav {
        val nav = SideNav()
        nav.addClassName(LumoUtility.Margin.Horizontal.MEDIUM)

        MenuConfiguration.getMenuEntries().forEach { entry ->
            nav.addItem(createSideNavItem(entry))
        }

        return nav
    }

    private fun createSideNavItem(menuEntry: MenuEntry): SideNavItem {
        return if (menuEntry.icon != null) {
            SideNavItem(menuEntry.title, menuEntry.path, Icon(menuEntry.icon))
        } else {
            SideNavItem(menuEntry.title, menuEntry.path)
        }
    }

    private fun createUserMenu(): Component {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? com.sms.entities.User

        val displayName = user?.person?.getFullName() ?: authentication?.name ?: "Staff"
        val roleNames = user?.roles?.joinToString(", ") { it.name } ?: "STAFF"

        val avatar = Avatar(displayName).apply {
            addThemeVariants(AvatarVariant.LUMO_XSMALL)
            addClassName(LumoUtility.Margin.MEDIUM)
            colorIndex = 2
        }

        val userMenu = MenuBar().apply {
            addThemeVariants(MenuBarVariant.LUMO_TERTIARY)
            addClassName(LumoUtility.Margin.MEDIUM)
        }

        val menu = userMenu.addItem(avatar)
        menu.add(displayName)

        menu.subMenu.addItem(roleNames).isEnabled = false

        menu.subMenu.addItem("Logout") {
            VaadinServletRequest.getCurrent().logout()
        }

        return userMenu
    }

    private fun registerStaffNotifications() {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? com.sms.entities.User
        val username = user?.username?.lowercase()?.trim() ?: return

        val uiRef = ui ?: return
        val session = uiRef.session
        val key = "staffNotificationListener"

        if (session.getAttribute(key) != null) return

        val listener: (String, Map<String, Any>) -> Unit = { eventType, data ->
            uiRef.access {
                when (eventType) {
                    "GENERAL_ALERT" -> showInteractiveNotification(
                        title = "Staff Alert",
                        message = data["message"] as? String ?: "New staff notification.",
                        variant = NotificationVariant.LUMO_CONTRAST
                    )
                }
            }
        }

        UiBroadcaster.registerForUser(username, listener)
        session.setAttribute(key, listener)

        uiRef.addDetachListener {
            UiBroadcaster.unregisterForUser(username, listener)
            session.setAttribute(key, null)
        }
    }
}