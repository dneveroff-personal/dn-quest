package dn.quest.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основной класс приложения Authentication Service
 *
 * Микросервис аутентификации для платформы DN Quest
 *
 * Основные функции:
 * - Регистрация и аутентификация пользователей
 * - Управление JWT токенами
 * - Восстановление пароля
 * - Управление ролями и правами
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@ComponentScan(basePackages = {
        "dn.quest.authentication",
        "dn.quest.shared"
})
public class AuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
}