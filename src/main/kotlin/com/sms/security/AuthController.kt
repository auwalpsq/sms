package com.sms.security

import com.sms.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val user = userService.findByUsername(request.username)
            ?: return ResponseEntity.badRequest().build()

        return if (passwordEncoder.matches(request.password, user.password)) {
            val token = JwtUtil.generateToken(user.username, user.roles.map { it.name })
            ResponseEntity.ok(LoginResponse(token))
        } else {
            ResponseEntity.status(401).build()
        }
    }
}