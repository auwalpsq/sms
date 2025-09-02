package com.sms.security

import com.sms.services.MyUserDetailsManager
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val userDetailsManager: MyUserDetailsManager
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            val username = JwtUtil.extractUsername(token)
            if (username != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsManager.findByUsername(username)
                if (userDetails != null && JwtUtil.validateToken(token)) {
                    val authorities = JwtUtil.extractRoles(token)
                        .map { SimpleGrantedAuthority("ROLE_$it") }

                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails.username, null, authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}