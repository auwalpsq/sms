package com.sms.security

import com.sms.services.MyUserDetailsManager
import com.vaadin.flow.server.auth.AnonymousAllowed
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userDetailsManager: MyUserDetailsManager,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val user = userDetailsManager.findByUsername(request.username)
            ?: return ResponseEntity.badRequest().build()

        return if (passwordEncoder.matches(request.password, user.password)) {
            val token = JwtUtil.generateToken(user.username, user.roles.map { it.name })
            ResponseEntity.ok(LoginResponse(token))
        } else {
            ResponseEntity.status(401).build()
        }
    }
}