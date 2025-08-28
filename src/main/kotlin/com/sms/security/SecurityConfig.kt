package com.sms.security

import com.sms.security.ui.view.LoginView
import com.sms.services.MyUserDetailsManager
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsManager: MyUserDetailsManager,
    private val passwordEncoder: PasswordEncoder, // now injected, not defined here!
    private val successHandler: RoleBasedAuthenticationSuccessHandler
) : VaadinWebSecurity() {

    override fun configure(http: HttpSecurity) {
        super.configure(http)

        http
            .formLogin { form ->
                form
                    .loginPage("/login")  // path handled by your LoginView
                    .permitAll()
                    .successHandler(successHandler)
            }
            .logout { logout ->
                logout.logoutSuccessUrl("/login?logout")
            }
            .exceptionHandling { exceptions ->
                exceptions.accessDeniedPage("/access-denied")
            }
        //setLoginView(http, LoginView::class.java)
    }

    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val builder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        builder
            .userDetailsService(userDetailsManager)
            .passwordEncoder(passwordEncoder)

        return builder.build()
    }
}