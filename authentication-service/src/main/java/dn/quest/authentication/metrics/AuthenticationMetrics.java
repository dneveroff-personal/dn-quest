package dn.quest.authentication.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Кастомные бизнес-метрики для сервиса аутентификации
 */
@Component
public class AuthenticationMetrics {

    private final Counter loginAttempts;
    private final Counter successfulLogins;
    private final Counter failedLogins;
    private final Counter passwordChangeAttempts;
    private final Counter successfulPasswordChanges;
    private final Counter failedPasswordChanges;
    private final Counter registrationAttempts;
    private final Counter successfulRegistrations;
    private final Counter failedRegistrations;
    private final Counter tokenRefreshAttempts;
    private final Counter successfulTokenRefreshes;
    private final Counter failedTokenRefreshes;
    private final Counter accountLockouts;
    private final Counter passwordResets;
    private final Counter profileUpdates;

    private final Timer loginDuration;
    private final Timer registrationDuration;
    private final Timer passwordChangeDuration;
    private final Timer tokenValidationDuration;

    private final AtomicLong activeUsersCount;
    private final AtomicLong lockedAccountsCount;
    private final AtomicLong sessionsCount;

    public AuthenticationMetrics(MeterRegistry meterRegistry) {
        // Счетчики для различных операций
        this.loginAttempts = Counter.builder("auth_login_attempts_total")
                .description("Общее количество попыток входа")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.successfulLogins = Counter.builder("auth_successful_logins_total")
                .description("Количество успешных входов")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.failedLogins = Counter.builder("auth_failed_logins_total")
                .description("Количество неудачных попыток входа")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.passwordChangeAttempts = Counter.builder("auth_password_change_attempts_total")
                .description("Общее количество попыток смены пароля")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.successfulPasswordChanges = Counter.builder("auth_successful_password_changes_total")
                .description("Количество успешных смен пароля")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.failedPasswordChanges = Counter.builder("auth_failed_password_changes_total")
                .description("Количество неудачных попыток смены пароля")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.registrationAttempts = Counter.builder("auth_registration_attempts_total")
                .description("Общее количество попыток регистрации")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.successfulRegistrations = Counter.builder("auth_successful_registrations_total")
                .description("Количество успешных регистраций")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.failedRegistrations = Counter.builder("auth_failed_registrations_total")
                .description("Количество неудачных попыток регистрации")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.tokenRefreshAttempts = Counter.builder("auth_token_refresh_attempts_total")
                .description("Общее количество попыток обновления токена")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.successfulTokenRefreshes = Counter.builder("auth_successful_token_refreshes_total")
                .description("Количество успешных обновлений токена")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.failedTokenRefreshes = Counter.builder("auth_failed_token_refreshes_total")
                .description("Количество неудачных попыток обновления токена")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.accountLockouts = Counter.builder("auth_account_lockouts_total")
                .description("Количество блокировок аккаунтов")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.passwordResets = Counter.builder("auth_password_resets_total")
                .description("Количество сбросов пароля")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.profileUpdates = Counter.builder("auth_profile_updates_total")
                .description("Количество обновлений профиля")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        // Таймеры для измерения времени выполнения операций
        this.loginDuration = Timer.builder("auth_login_duration_seconds")
                .description("Время выполнения операции входа")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.registrationDuration = Timer.builder("auth_registration_duration_seconds")
                .description("Время выполнения операции регистрации")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.passwordChangeDuration = Timer.builder("auth_password_change_duration_seconds")
                .description("Время выполнения смены пароля")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        this.tokenValidationDuration = Timer.builder("auth_token_validation_duration_seconds")
                .description("Время валидации токена")
                .tag("service", "authentication-service")
                .register(meterRegistry);

        // Счетчики для текущего состояния
        this.activeUsersCount = new AtomicLong(0);
        this.lockedAccountsCount = new AtomicLong(0);
        this.sessionsCount = new AtomicLong(0);

        // Регистрируем gauge метрики
        Gauge.builder("auth_active_users_count")
                .description("Количество активных пользователей")
                .tag("service", "authentication-service")
                .register(meterRegistry, this, AuthenticationMetrics::getActiveUsersCount);

        Gauge.builder("auth_locked_accounts_count")
                .description("Количество заблокированных аккаунтов")
                .tag("service", "authentication-service")
                .register(meterRegistry, this, AuthenticationMetrics::getLockedAccountsCount);

        Gauge.builder("auth_sessions_count")
                .description("Количество активных сессий")
                .tag("service", "authentication-service")
                .register(meterRegistry, this, AuthenticationMetrics::getSessionsCount);
    }

    // Методы для записи метрик

    public void recordLoginAttempt() {
        loginAttempts.increment();
    }

    public void recordSuccessfulLogin() {
        successfulLogins.increment();
    }

    public void recordFailedLogin(String reason) {
        failedLogins.increment(reason);
    }

    public void recordPasswordChangeAttempt() {
        passwordChangeAttempts.increment();
    }

    public void recordSuccessfulPasswordChange() {
        successfulPasswordChanges.increment();
    }

    public void recordFailedPasswordChange(String reason) {
        failedPasswordChanges.increment(reason);
    }

    public void recordRegistrationAttempt() {
        registrationAttempts.increment();
    }

    public void recordSuccessfulRegistration() {
        successfulRegistrations.increment();
    }

    public void recordFailedRegistration(String reason) {
        failedRegistrations.increment(reason);
    }

    public void recordTokenRefreshAttempt() {
        tokenRefreshAttempts.increment();
    }

    public void recordSuccessfulTokenRefresh() {
        successfulTokenRefreshes.increment();
    }

    public void recordFailedTokenRefresh(String reason) {
        failedTokenRefreshes.increment(reason);
    }

    public void recordAccountLockout() {
        accountLockouts.increment();
    }

    public void recordPasswordReset() {
        passwordResets.increment();
    }

    public void recordProfileUpdate() {
        profileUpdates.increment();
    }

    // Методы для измерения времени выполнения

    public Timer.Sample startLoginTimer() {
        return Timer.start();
    }

    public void recordLoginDuration(Timer.Sample sample) {
        sample.stop(loginDuration);
    }

    public Timer.Sample startRegistrationTimer() {
        return Timer.start();
    }

    public void recordRegistrationDuration(Timer.Sample sample) {
        sample.stop(registrationDuration);
    }

    public Timer.Sample startPasswordChangeTimer() {
        return Timer.start();
    }

    public void recordPasswordChangeDuration(Timer.Sample sample) {
        sample.stop(passwordChangeDuration);
    }

    public Timer.Sample startTokenValidationTimer() {
        return Timer.start();
    }

    public void recordTokenValidationDuration(Timer.Sample sample) {
        sample.stop(tokenValidationDuration);
    }

    // Удобные методы для измерения времени с лямбдами

    public <T> T timeLogin(TimedOperation<T> operation) {
        return loginDuration.recordCallable(operation::execute);
    }

    public <T> T timeRegistration(TimedOperation<T> operation) {
        return registrationDuration.recordCallable(operation::execute);
    }

    public <T> T timePasswordChange(TimedOperation<T> operation) {
        return passwordChangeDuration.recordCallable(operation::execute);
    }

    public <T> T timeTokenValidation(TimedOperation<T> operation) {
        return tokenValidationDuration.recordCallable(operation::execute);
    }

    // Методы для обновления счетчиков состояния

    public void setActiveUsersCount(long count) {
        activeUsersCount.set(count);
    }

    public void setLockedAccountsCount(long count) {
        lockedAccountsCount.set(count);
    }

    public void setSessionsCount(long count) {
        sessionsCount.set(count);
    }

    public void incrementActiveUsersCount() {
        activeUsersCount.incrementAndGet();
    }

    public void decrementActiveUsersCount() {
        activeUsersCount.decrementAndGet();
    }

    public void incrementLockedAccountsCount() {
        lockedAccountsCount.incrementAndGet();
    }

    public void decrementLockedAccountsCount() {
        lockedAccountsCount.decrementAndGet();
    }

    public void incrementSessionsCount() {
        sessionsCount.incrementAndGet();
    }

    public void decrementSessionsCount() {
        sessionsCount.decrementAndGet();
    }

    // Геттеры для gauge метрик

    private double getActiveUsersCount() {
        return activeUsersCount.get();
    }

    private double getLockedAccountsCount() {
        return lockedAccountsCount.get();
    }

    private double getSessionsCount() {
        return sessionsCount.get();
    }

    @FunctionalInterface
    public interface TimedOperation<T> {
        T execute() throws Exception;
    }
}