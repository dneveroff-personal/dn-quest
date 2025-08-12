package dn.quizengine.config;

import dn.quizengine.controller.service.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final AuthenticationService authService;

    public SecurityConfig(AuthenticationService authService) {
        this.authService = authService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(authService)
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // for POSTMAN
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/quizzes/ping").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/test").hasRole("USER")
                        .anyRequest().denyAll()
                );
        return http.build();
    }
}
