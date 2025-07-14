import com.sms.security.ui.view.LoginView
import com.sms.services.MyUserDetailsManager
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfig : VaadinWebSecurity() {

    override fun configure(http: HttpSecurity) {
        super.configure(http)

        setLoginView(http, LoginView::class.java)
    }
}
