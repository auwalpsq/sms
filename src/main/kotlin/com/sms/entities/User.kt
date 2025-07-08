package com.sms.entities

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    val email: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: Set<Role> = HashSet(),

    @OneToOne
    @JoinColumn(name = "person_id")
    val person: Person? = null,

    @Column(nullable = false)
    val enabled: Boolean = true,

    @Column(nullable = false)
    val accountNonExpired: Boolean = true,

    @Column(nullable = false)
    val accountNonLocked: Boolean = true,

    @Column(nullable = false)
    val credentialsNonExpired: Boolean = true
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { SimpleGrantedAuthority(it.name) }
    }

    override fun getPassword() = password
    override fun getUsername() = username
    override fun isEnabled() = enabled
    override fun isAccountNonExpired() = accountNonExpired
    override fun isAccountNonLocked() = accountNonLocked
    override fun isCredentialsNonExpired() = credentialsNonExpired
}

@Entity
@Table(name = "roles")
class Role(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String
)