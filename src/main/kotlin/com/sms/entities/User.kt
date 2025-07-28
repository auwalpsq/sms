package com.sms.entities

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    private val username: String,

    @Column(nullable = false)
    private var password: String,

    @Column(nullable = false)
    val email: String,


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    val roles: Set<Role> = HashSet(),

    @OneToOne
    @JoinColumn(name = "person_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
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