package com.sms.config

import com.sms.entities.Role
import com.sms.entities.User
import com.sms.mappers.RoleMapper
import com.sms.mappers.UserMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.security.crypto.password.PasswordEncoder

@Component
//@Profile("dev") // ✅ Runs only in `dev` profile
class DataInitializer(
    private val userMapper: UserMapper,
    private val roleMapper: RoleMapper,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val adminRole = roleMapper.findByName("ROLE_ADMIN")
            ?: Role(name = "ROLE_ADMIN", description = "Administrator").also {
                roleMapper.insertRole(it)
            }

        val userRole = roleMapper.findByName("ROLE_USER")
            ?: Role(name = "ROLE_USER", description = "Regular User").also {
                roleMapper.insertRole(it)
            }

        if (!userMapper.existsByUsername("admin")) {
            val admin = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                email = "admin@example.com",
                enabled = true,
                accountNonExpired = true,
                accountNonLocked = true,
                credentialsNonExpired = true
            )
            userMapper.insertUser(admin)
            roleMapper.addRoleToUser(admin.id, adminRole.id)
        }

        if (!userMapper.existsByUsername("user")) {
            val user = User(
                username = "user",
                password = passwordEncoder.encode("user123"),
                email = "user@example.com",
                enabled = true,
                accountNonExpired = true,
                accountNonLocked = true,
                credentialsNonExpired = true
            )
            userMapper.insertUser(user)
            roleMapper.addRoleToUser(user.id, userRole.id)
        }
    }
}
