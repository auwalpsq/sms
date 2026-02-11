package com.sms.ui.layout

import com.sms.entities.User
import com.sms.services.SchoolConfigService
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.AfterNavigationObserver
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.server.menu.MenuConfiguration
import org.springframework.security.core.context.SecurityContextHolder

class MainLayout(private val schoolConfigService: SchoolConfigService) :
        Composite<Div>(), RouterLayout, AfterNavigationObserver {

    private val navLinks = mutableListOf<Div>()
    private lateinit var contentArea: Div
    private lateinit var contentWrapper: Div

    override fun initContent(): Div {
        val root = Div().apply { addClassName("app-root") }

        val sidebar = buildSidebar()
        val header = buildHeader()

        contentArea = Div().apply { addClassName("app-content") }

        contentWrapper = Div(header, contentArea).apply { addClassName("content-wrapper") }

        // ORDER: sidebar | content
        root.add(sidebar, contentWrapper)

        return root
    }

    private fun buildHeader(): Div {
        val header = Div().apply { addClassName("app-header") }
        val schoolProfile = schoolConfigService.getSchoolProfile()

        val inner =
                Div().apply {
                    addClassName("header-inner")
                    // Logo Wrapper
                    val logoWrapper =
                            Div().apply {
                                addClassName("header-logo-wrapper")
                                // Placeholder logo or icon
                                add(
                                        Span().apply {
                                            addClassNames("ph", "ph-graduation-cap")
                                            style.set("color", "#5A3325")
                                            style.set("font-size", "24px")
                                        }
                                )
                            }

                    val title =
                            H3(schoolProfile.name).apply {
                                style.set("margin-left", "16px")
                                style.set("margin-top", "0")
                                style.set("margin-bottom", "0")
                                style.set("color", "#2b2623")
                            }

                    add(logoWrapper, title)

                    // User Menu (Right aligned)
                    val userMenu = createUserMenu()
                    userMenu.style.set("margin-left", "auto")
                    add(userMenu)
                }

        header.add(inner)
        return header
    }

    private fun buildSidebar(): Div {
        val side = Div().apply { addClassName("sidebar") }

        // Top generic avatar/home item similar to JL App
        val avatar = createAvatarItem()
        side.add(avatar)

        val scrollArea =
                Div().apply {
                    addClassName("sidebar-scroll")
                    style.set("width", "100%")
                }

        val menu =
                VerticalLayout().apply {
                    addClassName("sidebar-menu")
                    isPadding = false
                    isSpacing = true
                    alignItems = Alignment.CENTER
                }

        // Dynamically load menu entries from Vaadin's MenuConfiguration
        // This preserves the existing route discovery logic
        MenuConfiguration.getMenuEntries().forEach { entry ->
            // Use a default icon if none provided, or map logic
            val iconName = "ph-squares-four" // Default fallback
            // Note: Vaadin MenuEntry doesn't give us the Class<?> directly easily for RouterLink
            // with class
            // But we can use the path.

            // We need to construct the link manually.
            // We need to construct the link manually.
            val link = Div()
            link.addClickListener { UI.getCurrent().navigate(entry.path) }

            // Map Vaadin string to Phosphor class or use default
            val iconClass =
                    when (entry.icon) {
                        "vaadin:dashboard" -> "ph-squares-four"
                        "vaadin:users" -> "ph-users"
                        "vaadin:group" -> "ph-users-three"
                        "vaadin:wallet" -> "ph-wallet"
                        "vaadin:calendar" -> "ph-calendar"
                        "vaadin:folder-open" -> "ph-folder-open"
                        "vaadin:user" -> "ph-user"
                        "vaadin:form" -> "ph-scroll"
                        else -> "ph-circle"
                    }

            val icon = Span().apply { addClassNames("ph", iconClass, "nav-icon") }

            link.add(icon)
            link.addClassName("nav-link")
            link.element.setAttribute("data-route", entry.path)

            val text =
                    Span(entry.title).apply {
                        addClassName("nav-label")
                        element.setAttribute("title", entry.title)
                    }

            val wrapper = Div(link, text).apply { addClassName("nav-item") }
            navLinks.add(link)
            menu.add(wrapper)
        }

        scrollArea.add(menu)
        side.add(scrollArea)

        // Bottom settings/logout button
        val bottomBtn =
                Div().apply {
                    addClassName("bottom-settings-wrapper")
                    val link =
                            Div().apply {
                                addClassName("bottom-settings-link")
                                add(
                                        Span().apply {
                                            addClassNames("ph", "ph-power", "bottom-settings-icon")
                                            style.set("color", "white")
                                            style.set("font-size", "20px")
                                        }
                                )
                                addClickListener { VaadinServletRequest.getCurrent().logout() }
                            }
                    add(link)
                }
        side.add(bottomBtn)

        return side
    }

    private fun createAvatarItem(): Div {
        val icon =
                Span().apply {
                    addClassNames("ph", "ph-user", "profile-icon")
                    style.set("font-size", "24px")
                }

        val link =
                Div().apply {
                    add(icon)
                    addClassName("profile-link")
                }

        return Div(link).apply { addClassName("profile-wrapper") }
    }

    private fun createUserMenu(): Component {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? User
        val displayName = user?.person?.getFullName() ?: authentication?.name ?: "User"

        return Span(displayName).apply {
            addClassName("user-display-name")
            style.set("font-weight", "500")
            style.set("color", "#6E6E6E")
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        contentArea.removeAll()
        val component = content.element.component.orElse(null)
        if (component != null) contentArea.add(component)
        else contentArea.element.appendChild(content.element)
    }

    override fun afterNavigation(event: AfterNavigationEvent) {
        val current = event.location.path.trim('/')
        navLinks.forEach { link ->
            // Simple matching
            val route = link.element.getAttribute("data-route")?.trim('/') ?: ""
            if (route == current || (current.startsWith(route) && route.isNotEmpty())) {
                link.addClassName("active")
            } else {
                link.removeClassName("active")
            }
        }
    }
}
