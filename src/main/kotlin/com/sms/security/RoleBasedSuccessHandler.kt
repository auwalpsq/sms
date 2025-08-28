package com.sms.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class RoleBasedSuccessHandler : SavedRequestAwareAuthenticationSuccessHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val roles = authentication.authorities.map { it.authority }
        log.info("Login success for ${authentication.name}, roles=$roles")

        val target = when {
            roles.any { it.equals("ROLE_ADMIN", true) } -> "/admin"
            roles.any { it.equals("ROLE_TEACHER", true) } -> "/teacher"
            roles.any { it.equals("ROLE_GUARDIAN", true) } -> "/guardian"
            else -> "/"
        }

        // This respects saved request if any; otherwise uses the computed target.
        redirectStrategy.sendRedirect(request, response, target)
    }
}