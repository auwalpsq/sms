package com.sms.security.ui.view

import com.vaadin.flow.component.html.Main
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.theme.lumo.LumoUtility


@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed
class LoginView : Main(), BeforeEnterObserver {
    private val login: LoginForm

    init {
        addClassNames(
            LumoUtility.Display.FLEX,
            LumoUtility.JustifyContent.CENTER,
            LumoUtility.AlignItems.CENTER
        )
        setSizeFull()
        login = LoginForm()
        login.setAction("login")
        add(login)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")
        ) {
            login.setError(true)
        }
    }
}