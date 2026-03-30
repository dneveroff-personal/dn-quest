package dn.quest.authentication.service;

import dn.quest.authentication.dto.*;
import dn.quest.shared.dto.UserDTO;

/**
 * Сервис аутентификации пользователей
 */
public interface AuthService {

    /**
     * Регистрация нового пользователя
     */
    LoginResponseDTO register(RegisterRequestDTO request, String clientIp);

    /**
     * Вход пользователя в систему
     */
    LoginResponseDTO login(LoginRequestDTO request, String clientIp);

    /**
     * Обновление access токена с помощью refresh токена
     */
    LoginResponseDTO refreshToken(RefreshTokenRequestDTO request);

    /**
     * Выход пользователя из системы
     */
    void logout(String refreshToken);

    /**
     * Получение информации о профиле пользователя
     */
    UserDTO getProfile(String username);

    /**
     * Обновление профиля пользователя
     */
    UserDTO updateProfile(String username, UpdateProfileRequestDTO request);

    /**
     * Смена пароля пользователя
     */
    void changePassword(String username, ChangePasswordRequestDTO request);

    /**
     * Запрос на восстановление пароля
     */
    void forgotPassword(ForgotPasswordRequestDTO request);

    /**
     * Сброс пароля по токену
     */
    void resetPassword(ResetPasswordRequestDTO request);

    /**
     * Проверка валидности токена
     */
    boolean validateToken(String token);

    /**
     * Получение имени пользователя из токена
     */
    String extractUsername(String token);
}