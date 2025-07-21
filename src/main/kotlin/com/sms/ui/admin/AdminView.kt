package com.sms.ui.admin

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.theme.lumo.LumoUtility.*
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.menu.MenuConfiguration
import com.vaadin.flow.server.menu.MenuEntry
import jakarta.annotation.security.RolesAllowed

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
        logo.addClassNames(FontSize.LARGE, Margin.MEDIUM)
        addToNavbar(logo)
    }

    private fun createDrawer() {
        addToDrawer(createSideNav())
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
}