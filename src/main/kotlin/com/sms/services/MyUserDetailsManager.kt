package com.sms.services

import com.sms.entities.Person
import com.sms.entities.Role
import com.sms.entities.User
import com.sms.mappers.RoleMapper
import com.sms.mappers.UserMapper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder

@Service
class MyUserDetailsManager(
    private val userMapper: UserMapper,
    private val roleMapper: RoleMapper,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsManager {

    @Transactional
    override fun createUser(user: UserDetails) {
        if (userExists(user.username)) {
            throw IllegalArgumentException("User already exists: ${user.username}")
        }

        val newUser = (user as? User)?.copy(
            password = passwordEncoder.encode(user.password)
        ) ?: User(
            username = user.username,
            password = passwordEncoder.encode(user.password),
            email = "", // Set default or extract from UserDetails
            enabled = user.isEnabled,
            accountNonExpired = user.isAccountNonExpired,
            accountNonLocked = user.isAccountNonLocked,
            credentialsNonExpired = user.isCredentialsNonExpired
        )

        userMapper.insertUser(newUser)
    }

    @Transactional
    override fun updateUser(user: UserDetails) {
        val existingUser = loadUserByUsername(user.username) as User
        val updatedUser = existingUser.copy(
            password = passwordEncoder.encode(user.password),
            enabled = user.isEnabled,
            accountNonExpired = user.isAccountNonExpired,
            accountNonLocked = user.isAccountNonLocked,
            credentialsNonExpired = user.isCredentialsNonExpired
        )

        userMapper.updateUser(updatedUser)
    }

    @Transactional
    override fun deleteUser(username: String) {
        if (!userExists(username)) {
            throw UsernameNotFoundException("User not found: $username")
        }
        userMapper.deleteUser(username)
    }

    @Transactional
    override fun changePassword(oldPassword: String, newPassword: String) {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("No authentication found")

        val currentUsername = auth.name
        val user = loadUserByUsername(currentUsername) as User

        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw IllegalArgumentException("Old password does not match")
        }

        val updatedUser = user.copy(password = passwordEncoder.encode(newPassword))
        userMapper.updateUser(updatedUser)
    }


    override fun userExists(username: String): Boolean {
        return userMapper.existsByUsername(username)
    }

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userMapper.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        
        val roles = roleMapper.findRolesByUsername(username)
        return user.copy(
            roles = roles.toSet(),
            person = user.person
        )
    }

    @Transactional
    fun createUserWithRoles(
        username: String,
        password: String,
        email: String,
        roleNames: Set<String>,
        enabled: Boolean = true,
        person: Person
    ): User {
        if (userExists(username)) {
            throw IllegalArgumentException("User already exists: $username")
        }

        val roles = roleNames.map { roleName ->
            roleMapper.findByName(roleName) ?: run {
                val newRole = Role(name = roleName)
                roleMapper.insertRole(newRole)
                newRole
            }
        }.toSet()

        val user = User(
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            roles = roles,
            enabled = enabled,
            person = person
        )

        userMapper.insertUser(user)

        val userId = user.id
        roles.forEach { role -> roleMapper.addRoleToUser(userId, role.id) }

        return user
    }

    fun findAllUsers(): List<User> {
        return userMapper.findAllUsers().map { user ->
            val roles = roleMapper.findRolesByUsername(user.username)
            user.copy(roles = roles.toSet())
        }
    }
    fun findByUsername(username: String): User? {
        return userMapper.findByUsername(username)
    }

    @Transactional
    fun updateUserRoles(username: String, newRoles: Set<String>) {
        val user = userMapper.findByUsername(username)
            ?: throw IllegalArgumentException("User not found: $username")

        val userId = user.id

        // Remove all old roles
        roleMapper.removeAllRolesFromUser(userId)

        // Add new roles
        val roles = newRoles.map { roleName ->
            roleMapper.findByName(roleName) ?: run {
                val newRole = Role(name = roleName)
                roleMapper.insertRole(newRole)
                newRole
            }
        }

        roles.forEach { role ->
            roleMapper.addRoleToUser(userId, role.id)
        }
    }


}