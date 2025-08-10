package com.sms.ui.guardian

import com.sms.services.AcademicSessionService
import com.sms.ui.guardian.views.GuardianApplicationView
import com.sms.ui.guardian.views.GuardianProfileView
import com.sms.util.launchUiCoroutine
import com.sms.util.withUi
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.avatar.AvatarVariant
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.menubar.MenuBarVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.theme.lumo.LumoUtility.*
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.server.menu.MenuConfiguration
import com.vaadin.flow.server.menu.MenuEntry
import com.vaadin.flow.theme.lumo.LumoUtility
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("Guardian Portal")
@Route(value = "guardian")
@RolesAllowed("GUARDIAN")
class GuardianLayout(
    private val sessionService: AcademicSessionService
) : AppLayout() {

    private val ui: UI = UI.getCurrent()
    init {
        createHeader()
        createDrawer()
    }

    private fun createHeader() {
        val logo = H3("Guardian Portal")
        val layout = HorizontalLayout()
        layout.setWidthFull()
        layout.addClassNames(
            LumoUtility.JustifyContent.BETWEEN,
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Padding.Horizontal.MEDIUM
        )

        // Add logo on the left
        layout.add(logo)

        // Fetch and add session info on the right
        launchUiCoroutine {
            val session = sessionService.findCurrent()
            ui?.withUi {
                val sessionInfo = if (session != null) {
                    Span("${session.displaySession} - ${session.term} Term")
                } else {
                    Span("No active academic session")
                }
                sessionInfo.addClassNames(
                    LumoUtility.FontWeight.BOLD,
                    LumoUtility.FontSize.MEDIUM
                )

                // Add session info to the layout (right side)
                layout.add(sessionInfo)
            }
        }

        // Add the layout to the navbar
        addToNavbar(layout)
    }

    private fun createDrawer() {
        addToDrawer(Scroller(createSideNav()), createUserMenu())
    }

    private fun createSideNav(): SideNav {
        val nav = SideNav()
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM)
        MenuConfiguration.getMenuEntries().forEach { entry -> nav.addItem(createSideNavItem(entry)) }

        return nav
    }
    private fun createSideNavItem (menuEntry : MenuEntry) : SideNavItem {
        if(menuEntry.icon != null){
            return SideNavItem(menuEntry.title, menuEntry.path, Icon(menuEntry.icon))
        }else{
            return SideNavItem(menuEntry.title, menuEntry.path)
        }
    }

    private fun createUserMenu(): Component {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? com.sms.entities.User

        val displayName = user?.person?.getFullName() ?: authentication?.name ?: "Guardian"
        val roleNames = user?.roles?.joinToString(", ") { it.name } ?: "GUARDIAN"

        val avatar = Avatar(displayName)
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL)
        avatar.addClassNames(Margin.MEDIUM)
        avatar.colorIndex = 5

        val userMenu = MenuBar()
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY)
        userMenu.addClassNames(Margin.MEDIUM)

        val userMenuItem = userMenu.addItem(avatar)
        userMenuItem.add(displayName)
        userMenuItem.subMenu.addItem(roleNames).isEnabled = false
        userMenuItem.subMenu.addItem("View Profile").apply {
            addClickListener {
                UI.getCurrent().navigate(GuardianProfileView::class.java)
            }
        }
        userMenuItem.subMenu.addItem("Change Password").isEnabled = false // Implement as needed
        userMenuItem.subMenu.addItem("Logout") {
            VaadinServletRequest.getCurrent().logout()
            //UI.getCurrent().page.setLocation("/login")
        }

        return userMenu
    }
}