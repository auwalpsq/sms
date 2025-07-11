package com.sms.config

import com.sms.entities.Role
import com.sms.entities.User
import com.sms.mappers.RoleMapper
import com.sms.mappers.UserMapper
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import org.springframework.security.crypto.password.PasswordEncoder

@Component
class DataInitializer(
    private val userMapper: UserMapper,
    private val roleMapper: RoleMapper,
    private val passwordEncoder: PasswordEncoder
) {

    @PostConstruct
    fun initData() {
        val adminRole = roleMapper.findByName("ROLE_ADMIN")
            ?: roleMapper.insertRole(Role(name = "ROLE_ADMIN", description = "Administrator"))

        val userRole = roleMapper.findByName("ROLE_USER")
            ?: roleMapper.insertRole(Role(name = "ROLE_USER", description = "Regular User"))

        if (!userMapper.existsByUsername("admin")) {
            val admin = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                email = "admin@example.com",
                roles = setOf(adminRole).filterIsInstance<Role>().toSet()
            )
            userMapper.insertUser(admin)
        }

        if (!userMapper.existsByUsername("user")) {
            val user = User(
                username = "user",
                password = passwordEncoder.encode("user123"),
                email = "user@example.com",
                roles = setOf(userRole).filterIsInstance<Role>().toSet()
            )
            userMapper.insertUser(user)
        }
    }
}
