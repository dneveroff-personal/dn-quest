package dn.quest.filestorage.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dn.quest.filestorage.client.AuthenticationServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация безопасности для File Storage Service
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationServiceClient authServiceClient;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.debug("Загрузка данных пользователя: {}", username);
            try {
                // Получаем данные пользователя через Authentication Service
                var userResponse = authServiceClient.getUserByUsername(username);
                
                return org.springframework.security.core.userdetails.User.builder()
                        .username(userResponse.getUsername())
                        .password("") // Пароль не нужен для валидации токена
                        .authorities(userResponse.getRoles().stream()
                                .map(role -> "ROLE_" + role)
                                .toArray(String[]::new))
                        .accountLocked(!userResponse.getIsActive())
                        .accountExpired(false)
                        .credentialsExpired(false)
                        .disabled(!userResponse.getIsActive())
                        .build();
            } catch (Exception e) {
                log.warn("Пользователь не найден: {}", username);
                throw new UsernameNotFoundException("Пользователь не найден: " + username);
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; media-src 'self' blob:;")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты для скачивания файлов (если файл публичный)
                        .requestMatchers(HttpMethod.GET, "/api/files/download/**", "/api/files/public/**")
                        .permitAll()
                        
                        // Эндпоинты документации
                        .requestMatchers("/api-docs/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        
                        // Эндпоинты мониторинга
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics")
                        .permitAll()
                        
                        // Эндпоинты требующие аутентификации
                        .requestMatchers("/api/files/upload", "/api/files/batch-upload")
                        .authenticated()
                        
                        // Эндпоинты для управления файлами
                        .requestMatchers(HttpMethod.DELETE, "/api/files/**")
                        .authenticated()
                        
                        // Эндпоинты для получения метаданных
                        .requestMatchers(HttpMethod.GET, "/api/files/**")
                        .authenticated()
                        
                        // Административные эндпоинты
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        
                        // Все остальные запросы требуют аутентификации
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security конфигурация File Storage Service инициализирована");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("X-Username", "Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}