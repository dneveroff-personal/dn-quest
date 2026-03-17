package dn.quest.authentication.controller;

import dn.quest.authentication.dto.*;
import dn.quest.authentication.metrics.AuthenticationMetrics;
import dn.quest.authentication.service.AuthService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер аутентификации пользователей
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Аутентификация", description = "API для аутентификации и управления пользователями")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationMetrics authenticationMetrics;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует")
    })
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        authenticationMetrics.recordRegistrationAttempt();
        Timer.Sample timer = authenticationMetrics.startRegistrationTimer();
        
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при регистрации: {}", bindingResult.getAllErrors());
            authenticationMetrics.recordFailedRegistration("validation_error");
            authenticationMetrics.recordRegistrationDuration(timer);
            return ResponseEntity.badRequest().body(createErrorResponse("Ошибка валидации", bindingResult));
        }

        String clientIp = getClientIp(httpRequest);
        
        try {
            LoginResponseDTO response = authService.register(request, clientIp);
            log.info("Пользователь {} успешно зарегистрирован с IP: {}", request.getUsername(), clientIp);
            authenticationMetrics.recordSuccessfulRegistration();
            authenticationMetrics.incrementActiveUsersCount();
            authenticationMetrics.recordRegistrationDuration(timer);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка регистрации пользователя {} с IP: {}", request.getUsername(), clientIp, e);
            authenticationMetrics.recordFailedRegistration("user_exists");
            authenticationMetrics.recordRegistrationDuration(timer);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и возвращает JWT токены")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequestDTO request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        authenticationMetrics.recordLoginAttempt();
        Timer.Sample timer = authenticationMetrics.startLoginTimer();
        
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при входе: {}", bindingResult.getAllErrors());
            authenticationMetrics.recordFailedLogin("validation_error");
            authenticationMetrics.recordLoginDuration(timer);
            return ResponseEntity.badRequest().body(createErrorResponse("Ошибка валидации", bindingResult));
        }

        String clientIp = getClientIp(httpRequest);
        
        try {
            LoginResponseDTO response = authService.login(request, clientIp);
            log.info("Пользователь {} успешно вошел в систему с IP: {}", request.getUsername(), clientIp);
            authenticationMetrics.recordSuccessfulLogin();
            authenticationMetrics.incrementSessionsCount();
            authenticationMetrics.recordLoginDuration(timer);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Неудачная попытка входа для пользователя {} с IP: {}", request.getUsername(), clientIp);
            authenticationMetrics.recordFailedLogin("invalid_credentials");
            authenticationMetrics.recordLoginDuration(timer);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление токена", description = "Обновляет access токен с помощью refresh токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверный refresh токен"),
            @ApiResponse(responseCode = "401", description = "Refresh токен истек")
    })
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        authenticationMetrics.recordTokenRefreshAttempt();
        try {
            LoginResponseDTO response = authService.refreshToken(request);
            log.debug("Токен успешно обновлен");
            authenticationMetrics.recordSuccessfulTokenRefresh();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка обновления токена", e);
            authenticationMetrics.recordFailedTokenRefresh("invalid_token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход из системы", description = "Завершает сессию пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный выход"),
            @ApiResponse(responseCode = "400", description = "Ошибка запроса")
    })
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> request) {
        String refreshToken = request != null ? request.get("refreshToken") : null;
        authService.logout(refreshToken);
        log.info("Пользователь вышел из системы");
        authenticationMetrics.decrementSessionsCount();
        return ResponseEntity.ok(Map.of("message", "Выход выполнен успешно"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Получение профиля", description = "Возвращает информацию о текущем пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль получен",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<?> getProfile(
            @Parameter(description = "Имя пользователя из JWT токена") 
            @RequestHeader("X-Username") String username) {
        try {
            UserDTO profile = authService.getProfile(username);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "Обновление профиля", description = "Обновляет информацию о пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профиль обновлен",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Email уже используется")
    })
    public ResponseEntity<?> updateProfile(
            @Parameter(description = "Имя пользователя из JWT токена")
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody UpdateProfileRequestDTO request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Ошибка валидации", bindingResult));
        }

        try {
            UserDTO updatedProfile = authService.updateProfile(username, request);
            log.info("Профиль пользователя {} обновлен", username);
            authenticationMetrics.recordProfileUpdate();
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка обновления профиля пользователя {}", username, e);
            if (e.getMessage().contains("уже существует")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Смена пароля", description = "Изменяет пароль текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "401", description = "Неверный текущий пароль"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<?> changePassword(
            @Parameter(description = "Имя пользователя из JWT токена")
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody ChangePasswordRequestDTO request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Ошибка валидации", bindingResult));
        }

        authenticationMetrics.recordPasswordChangeAttempt();
        Timer.Sample timer = authenticationMetrics.startPasswordChangeTimer();
        
        try {
            authService.changePassword(username, request);
            log.info("Пароль пользователя {} изменен", username);
            authenticationMetrics.recordSuccessfulPasswordChange();
            authenticationMetrics.recordPasswordChangeDuration(timer);
            return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка смены пароля пользователя {}", username, e);
            authenticationMetrics.recordFailedPasswordChange("invalid_current_password");
            authenticationMetrics.recordPasswordChangeDuration(timer);
            if (e.getMessage().contains("Неверный текущий пароль")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Восстановление пароля", description = "Отправляет инструкцию по восстановлению пароля на email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Инструкция отправлена"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Ошибка валидации", bindingResult));
        }

        authService.forgotPassword(request);
        log.info("Запрос на восстановление пароля отправлен для email: {}", request.getEmail());
        authenticationMetrics.recordPasswordReset();
        return ResponseEntity.ok(Map.of("message", "Инструкция по восстановлению пароля отправлена на указанный email"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Сброс пароля", description = "Устанавливает новый пароль по токену сброса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно сброшен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            @ApiResponse(responseCode = "401", description = "Неверный или истекший токен")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Ошибка валидации", bindingResult));
        }

        try {
            authService.resetPassword(request);
            log.info("Пароль успешно сброшен по токену");
            authenticationMetrics.recordPasswordReset();
            return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка сброса пароля", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Валидация токена", description = "Проверяет валидность JWT токена")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен валиден"),
            @ApiResponse(responseCode = "401", description = "Токен невалиден")
    })
    public ResponseEntity<?> validateToken(@Parameter(description = "JWT токен") @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }

        String token = authHeader.substring(7);
        Timer.Sample timer = authenticationMetrics.startTokenValidationTimer();
        boolean isValid = authService.validateToken(token);
        authenticationMetrics.recordTokenValidationDuration(timer);
        
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Map<String, Object> createErrorResponse(String message, BindingResult bindingResult) {
        Map<String, Object> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage()
                ));
        
        return Map.of(
                "message", message,
                "errors", errors
        );
    }
}