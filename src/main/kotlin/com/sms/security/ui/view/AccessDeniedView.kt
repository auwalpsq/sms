package com.sms.security.ui.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.theme.lumo.LumoUtility.*
import jakarta.annotation.security.PermitAll

@PermitAll
@Route("access-denied")
@PageTitle("Access Denied")
class AccessDeniedView : VerticalLayout() {

    init {
        setSizeFull()
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        addClassNames(Padding.LARGE, Gap.LARGE)

        val icon = Icon(VaadinIcon.LOCK)
        icon.setSize("64px")
        icon.style.set("color", "var(--lumo-error-color)")

        val heading = H2("Access Denied")
        val message = Paragraph("You do not have permission to view this page.")

        val backButton = RouterLink("Go to Login", com.sms.security.ui.view.LoginView::class.java)
        backButton.addClassNames(Margin.Top.LARGE, TextColor.PRIMARY)

        add(icon, heading, message, backButton)
    }
}
