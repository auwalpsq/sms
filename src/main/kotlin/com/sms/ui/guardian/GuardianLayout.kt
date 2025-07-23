package com.sms.ui.guardian

import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.avatar.AvatarVariant
import com.vaadin.flow.component.html.H1
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
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("Guardian Portal")
@Route(value = "guardian")
@RolesAllowed("GUARDIAN")
class GuardianLayout : AppLayout() {

    init {
        createHeader()
        createDrawer()
    }

    private fun createHeader() {
        val logo = H1("Guardian Portal")
        val layout = HorizontalLayout(logo)
        layout.addClassNames(JustifyContent.CENTER, Width.FULL)
        logo.addClassNames(FontSize.LARGE, Margin.MEDIUM)
        addToNavbar(layout)
    }

    private fun createDrawer() {
        addToDrawer(Scroller(createSideNav()), createUserMenu())
    }

    private fun createSideNav(): SideNav {
        val nav = SideNav()
        nav.addClassNames(Margin.Horizontal.MEDIUM)

        // Add guardian-specific navigation items
//        nav.addItem(
//            SideNavItem("Dashboard", GuardianDashboardView::class.java, VaadinIcon.HOME.create())
//        )
        nav.addItem(
            SideNavItem("My Profile", GuardianProfileView::class.java, VaadinIcon.USER.create())
        )
//        nav.addItem(
//            SideNavItem("My Students", GuardianStudentsView::class.java, VaadinIcon.ACADEMY_CAP.create())
//        )
//        nav.addItem(
//            SideNavItem("Apply for Admission", GuardianAdmissionView::class.java, VaadinIcon.PLUS_CIRCLE.create())
//        )
//        nav.addItem(
//            SideNavItem("Payments", GuardianPaymentsView::class.java, VaadinIcon.MONEY.create())
//        )
//        nav.addItem(
//            SideNavItem("Messages", GuardianMessagesView::class.java, VaadinIcon.ENVELOPE.create())
//        )

        return nav
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
            UI.getCurrent().page.setLocation("/login")
        }

        return userMenu
    }
}