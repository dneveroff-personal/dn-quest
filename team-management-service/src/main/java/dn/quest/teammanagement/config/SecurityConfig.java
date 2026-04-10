package dn.quest.teammanagement.config;

import dn.quest.shared.config.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация безопасности для Team Management Service
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(auth -> auth
                        // Эндпоинты документации
                        .requestMatchers("/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        
                        // Эндпоинты мониторинга - РАЗРЕШЕНЫ БЕЗ АУТЕНТИФИКАЦИИ
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus")
                        .permitAll()
                        
                        // Публичные API эндпоинты команды (без аутентификации для внешних вызовов)
                        .requestMatchers("/api/teams/public/**", "/api/invitations/accept/**")
                        .permitAll()
                        
                        // Все остальные запросы требуют аутентификации
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security конфигурация инициализирована для team-management-service");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("X-Username", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}