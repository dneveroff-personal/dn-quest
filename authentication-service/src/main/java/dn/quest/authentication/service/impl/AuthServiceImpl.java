package dn.quest.authentication.service.impl;

import dn.quest.authentication.dto.*;
import dn.quest.authentication.entity.User;
import dn.quest.authentication.service.AuthService;
import dn.quest.authentication.service.UserService;
import dn.quest.shared.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация сервиса аутентификации
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;
    
    @Value("${jwt.issuer:dn-quest-auth-service}")
    private String jwtIssuer;

    @Override
    public LoginResponseDTO register(RegisterRequestDTO request, String clientIp) {
        log.info("Регистрация нового пользователя: {} с IP: {}", request.getUsername(), clientIp);
        
        // Проверка существования пользователя
        if (userService.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        
        if (request.getEmail() != null && userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        
        // Создание пользователя
        User user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getPublicName(),
                dn.quest.shared.enums.UserRole.PLAYER
        );
        
        // Генерация токенов
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        
        String accessToken = JwtUtil.generateAccessToken(jwtSecret, user.getUsername(), claims, accessTokenExpiration);
        String refreshToken = JwtUtil.generateRefreshToken(jwtSecret, user.getUsername(), refreshTokenExpiration);
        
        // Сохранение refresh токена
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        userService.updateUser(user);
        
        log.info("Пользователь успешно зарегистрирован: {} с ID: {}", user.getUsername(), user.getId());
        
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .expiresAt(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
                .user(userService.toDTOWithPermissions(user))
                .build();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request, String clientIp) {
        log.debug("Попытка входа пользователя: {} с IP: {}", request.getUsername(), clientIp);
        
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {} с IP: {}", request.getUsername(), clientIp);
                    return new IllegalArgumentException("Неверные учетные данные");
                });
        
        if (!user.getIsActive()) {
            log.warn("Попытка входа неактивного пользователя: {} с IP: {}", request.getUsername(), clientIp);
            throw new IllegalArgumentException("Пользователь заблокирован");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Неверный пароль для пользователя: {} с IP: {}", request.getUsername(), clientIp);
            throw new IllegalArgumentException("Неверные учетные данные");
        }
        
        // Генерация токенов
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        
        String accessToken = JwtUtil.generateAccessToken(jwtSecret, user.getUsername(), claims, accessTokenExpiration);
        String refreshToken = JwtUtil.generateRefreshToken(jwtSecret, user.getUsername(), refreshTokenExpiration);
        
        // Обновление refresh токена и времени последнего входа
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        user.setLastLoginAt(Instant.now());
        userService.updateUser(user);
        
        log.info("Пользователь успешно вошел в систему: {} с IP: {}", user.getUsername(), clientIp);
        
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .expiresAt(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
                .user(userService.toDTOWithPermissions(user))
                .build();
    }

    @Override
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        log.debug("Обновление токена");
        
        // Валидация refresh токена
        if (!JwtUtil.isTokenValid(request.getRefreshToken(), jwtSecret)) {
            throw new IllegalArgumentException("Неверный refresh токен");
        }
        
        String username = JwtUtil.extractSubject(request.getRefreshToken(), jwtSecret);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        if (!request.getRefreshToken().equals(user.getRefreshToken()) || user.isRefreshTokenExpired()) {
            throw new IllegalArgumentException("Refresh токен истек или недействителен");
        }
        
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Пользователь заблокирован");
        }
        
        // Генерация новых токенов
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        
        String newAccessToken = JwtUtil.generateAccessToken(jwtSecret, user.getUsername(), claims, accessTokenExpiration);
        String newRefreshToken = JwtUtil.generateRefreshToken(jwtSecret, user.getUsername(), refreshTokenExpiration);
        
        // Обновление refresh токена
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        userService.updateUser(user);
        
        log.debug("Токен успешно обновлен для пользователя: {}", user.getUsername());
        
        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .expiresAt(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
                .user(userService.toDTOWithPermissions(user))
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        log.debug("Выход пользователя из системы");
        
        if (refreshToken != null) {
            try {
                String username = JwtUtil.extractSubject(refreshToken, jwtSecret);
                User user = userService.findByUsername(username).orElse(null);
                
                if (user != null && refreshToken.equals(user.getRefreshToken())) {
                    user.clearRefreshToken();
                    userService.updateUser(user);
                    log.info("Пользователь вышел из системы: {}", username);
                }
            } catch (Exception e) {
                log.warn("Ошибка при обработке выхода из системы", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfile(String username) {
        User user = userService.findByUsernameWithPermissions(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        return userService.toDTOWithPermissions(user);
    }

    @Override
    public UserDTO updateProfile(String username, UpdateProfileRequestDTO request) {
        log.debug("Обновление профиля пользователя: {}", username);
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        // Обновление полей
        if (request.getPublicName() != null) {
            user.setPublicName(request.getPublicName());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userService.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            user.setEmail(request.getEmail());
            user.setIsEmailVerified(false); // Требуется повторная верификация
        }
        
        User updatedUser = userService.updateUser(user);
        log.info("Профиль пользователя обновлен: {}", username);
        
        return userService.toDTOWithPermissions(updatedUser);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequestDTO request) {
        log.debug("Смена пароля пользователя: {}", username);
        
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        // Проверка текущего пароля
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }
        
        // Установка нового пароля
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now());
        
        // Очистка всех refresh токенов для принудительного повторного входа
        user.clearRefreshToken();
        
        userService.updateUser(user);
        log.info("Пароль пользователя изменен: {}", username);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        log.debug("Запрос на восстановление пароля для email: {}", request.getEmail());
        
        User user = userService.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user != null) {
            // Генерация токена сброса пароля
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiresAt(Instant.now().plusMillis(24 * 60 * 60 * 1000)); // 24 часа
            userService.updateUser(user);
            
            log.info("Сгенерирован токен сброса пароля для пользователя: {}", user.getUsername());
            // TODO: Отправка email с токеном сброса пароля
        }
        
        // Всегда возвращаем успех для безопасности
        log.info("Запрос на восстановление пароля обработан для email: {}", request.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO request) {
        log.debug("Сброс пароля по токену");
        
        User user = userService.findByPasswordResetToken(request.getToken())
                .orElse(null);
        
        if (user == null || user.isPasswordResetTokenExpired()) {
            throw new IllegalArgumentException("Неверный или истекший токен сброса пароля");
        }
        
        // Установка нового пароля
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.clearPasswordResetToken();
        user.clearRefreshToken(); // Принудительный выход со всех устройств
        user.setUpdatedAt(Instant.now());
        
        userService.updateUser(user);
        log.info("Пароль успешно сброшен для пользователя: {}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            return JwtUtil.isTokenValid(token, jwtSecret);
        } catch (Exception e) {
            log.debug("Ошибка валидации токена", e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String extractUsername(String token) {
        try {
            return JwtUtil.extractSubject(token, jwtSecret);
        } catch (Exception e) {
            log.debug("Ошибка извлечения имени пользователя из токена", e);
            return null;
        }
    }
}