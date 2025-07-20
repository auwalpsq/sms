package com.sms.ui.admin

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.theme.lumo.LumoUtility.*
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
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
        val layout = VerticalLayout()
        layout.addClassNames(Padding.SMALL)

        layout.add(
            createNavLink("Guardians", "admin/guardian", VaadinIcon.USER.create()),
//            createNavLink("Applications", "admin/application", VaadinIcon.CLIPBOARD_TEXT.create()),
//            createNavLink("Students", "admin/student", VaadinIcon.GROUP.create()),
//            createNavLink("Settings", "admin/setting", VaadinIcon.COG.create())
        )

        addToDrawer(layout)
    }

    private fun createNavLink(text: String, route: String, icon: Icon): RouterLink {
        val link = RouterLink(text, Class.forName("com.sms.ui.admin." + route.removePrefix("admin/").replaceFirstChar { it.uppercase() } + "View") as Class<out com.vaadin.flow.component.Component>)
        link.add(icon)
        link.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.SMALL)
        return link
    }
}