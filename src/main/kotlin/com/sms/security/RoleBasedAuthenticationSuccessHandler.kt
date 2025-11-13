package com.sms.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class RoleBasedAuthenticationSuccessHandler : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val roles = authentication.authorities.map { it.authority }

        val targetUrl = when {
            "ROLE_ADMIN" in roles -> "/admin"
            "ROLE_STAFF" in roles -> "/staff"
            "ROLE_GUARDIAN" in roles -> "/guardian"
            else -> "/access-denied"
        }

        response.sendRedirect(targetUrl)
    }
}