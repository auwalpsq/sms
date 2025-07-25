package com.sms.ui.admin

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.avatar.Avatar
import com.vaadin.flow.component.avatar.AvatarVariant
import com.vaadin.flow.component.html.H1
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
import jakarta.annotation.security.RolesAllowed
import org.springframework.security.core.context.SecurityContextHolder

@PageTitle("Admin Dashboard")
@Route(value = "admin")
@RolesAllowed("ADMIN")
class AdminView : AppLayout() {

    init {
        createHeader()
        createDrawer()
    }

    private fun createHeader() {
        val logo = H1("School Management System")
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
        MenuConfiguration.getMenuEntries().forEach { entry -> nav.addItem(createSideNavItem(entry)) }

        return nav
    }
    private fun createSideNavItem (menuEntry : MenuEntry) : SideNavItem{
        if(menuEntry.icon != null){
            return SideNavItem(menuEntry.title, menuEntry.path, Icon(menuEntry.icon))
        }else{
            return SideNavItem(menuEntry.title, menuEntry.path)
        }
    }

    private fun createUserMenu() : Component{
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? com.sms.entities.User

        val displayName = user?.person?.getFullName() ?: authentication?.name ?: "Unknown"
        val roleNames = user?.roles?.joinToString(", ") { it.name } ?: ""

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
        userMenuItem.subMenu.addItem("View Profile").isEnabled = false
        userMenuItem.subMenu.addItem("Manage Settings").isEnabled = false
        userMenuItem.subMenu.addItem("Logout"){
            VaadinServletRequest.getCurrent().logout()
            //UI.getCurrent().page.setLocation("/login")
        }

        return userMenu
    }
}