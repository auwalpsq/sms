package com.sms.config

import com.sms.entities.Role
import com.sms.entities.User
import com.sms.mappers.RoleMapper
import com.sms.mappers.UserMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    private val userMapper: UserMapper,
    private val roleMapper: RoleMapper,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun initData(): CommandLineRunner = CommandLineRunner {
        // Create roles
        val adminRole = roleMapper.findByName("ROLE_ADMIN") ?:
        roleMapper.insertRole(Role(name = "ROLE_ADMIN", description = "Administrator"))

        val userRole = roleMapper.findByName("ROLE_USER") ?:
        roleMapper.insertRole(Role(name = "ROLE_USER", description = "Regular User"))

        // Create admin user
        if (!userMapper.existsByUsername("admin")) {
            val admin = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                email = "admin@example.com",
                roles = setOf(adminRole).filterIsInstance<Role>().toSet()
            )
            userMapper.insertUser(admin)
        }

        // Create regular user
        if (!userMapper.existsByUsername("user")) {
            val user = User(
                username = "user",
                password = passwordEncoder.encode("user123"),
                email = "user@example.com",
                roles = setOf(adminRole).filterIsInstance<Role>().toSet()
            )
            userMapper.insertUser(user)
        }
    }
}