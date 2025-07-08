package com.sms.security


import com.sms.security.ui.view.LoginView
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager

@EnableWebSecurity
@Configuration
internal class SecurityConfig : VaadinWebSecurity() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity?) {
        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }

    @Bean
    fun userDetailsManager(): UserDetailsManager {
        LoggerFactory.getLogger(SecurityConfig::class.java)
            .warn("Using in-memory user details manager!")
        val user = User.withUsername("user")
            .password("{noop}user")
            .roles("USER")
            .build()
        val admin = User.withUsername("admin")
            .password("{noop}admin")
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(user, admin)
    }
}