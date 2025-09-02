package com.sms.security

import com.sms.services.MyUserDetailsManager
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
@Order(1)
class VaadinSecurityConfig(
    private val userDetailsManager: MyUserDetailsManager,
    private val passwordEncoder: PasswordEncoder,
    private val successHandler: RoleBasedAuthenticationSuccessHandler
) : VaadinWebSecurity() {

    override fun configure(http: HttpSecurity) {
        super.configure(http)

        http
            .formLogin { form ->
                form.loginPage("/login")
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
        builder.userDetailsService(userDetailsManager)
            .passwordEncoder(passwordEncoder)

        return builder.build()
    }
}