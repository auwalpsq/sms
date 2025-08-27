package com.sms.security

import com.sms.security.ui.view.LoginView
import com.sms.services.MyUserDetailsManager
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration::class)
class SecurityConfig(
    private val userDetailsManager: MyUserDetailsManager,
    private val passwordEncoder: PasswordEncoder,
    private val successHandler: RoleBasedAuthenticationSuccessHandler,
    private val jwtFilter: JwtFilter
) {

    /**
     * API Security (JWT, stateless)
     */
    @Bean
    @Order(1)
    fun apiSecurity(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/login").permitAll()
                    .requestMatchers("/api/**").authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * Vaadin UI Security (form login, session-based)
     */
    @Bean
    @Order(2)
    fun uiSecurity(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() } // Vaadin handles CSRF automatically
            .authorizeHttpRequests { auth ->
                // Only allow login page and static assets publicly
                auth.requestMatchers("/VAADIN/**", "/images/**").permitAll()
            }
            .formLogin { form ->
                form.loginPage("/login") // Vaadin login view also works
                    .successHandler(successHandler)
            }
            .logout { logout ->
                logout.logoutSuccessUrl("/login?logout")
            }
            .exceptionHandling { exceptions ->
                exceptions.accessDeniedPage("/access-denied")
            }

        // VaadinSecurityConfigurer applied last
        return http.with(VaadinSecurityConfigurer.vaadin()) { configurer ->
            configurer.loginView(LoginView::class.java)

        }.build()
    }


    /**
     * Shared AuthenticationManager
     */
    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val builder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        builder
            .userDetailsService(userDetailsManager)
            .passwordEncoder(passwordEncoder)
        return builder.build()
    }
}