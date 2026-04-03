package dn.quest.usermanagement.service;

import dn.quest.usermanagement.dto.UpdateSettingsRequestDTO;
import dn.quest.usermanagement.dto.UserSettingsDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления настройками пользователей
 */
public interface UserSettingsService {

    /**
     * Создает настройки пользователя по умолчанию
     */
    UserSettingsDTO createUserSettings(UUID userId);

    /**
     * Получает настройки пользователя по ID пользователя
     */
    Optional<UserSettingsDTO> getUserSettingsByUserId(UUID userId);

    /**
     * Получает настройки пользователя по ID настроек
     */
    Optional<UserSettingsDTO> getUserSettingsById(Long id);

    /**
     * Обновляет настройки пользователя
     */
    UserSettingsDTO updateUserSettings(UUID userId, UpdateSettingsRequestDTO request);

    /**
     * Обновляет настройки приватности
     */
    UserSettingsDTO updatePrivacySettings(UUID userId, Boolean profilePublic, Boolean showEmail, 
                                         Boolean showRealName, Boolean showLocation, 
                                         Boolean showWebsite, Boolean showStatistics);

    /**
     * Обновляет настройки уведомлений
     */
    UserSettingsDTO updateNotificationSettings(UUID userId, Boolean emailNotifications, 
                                              Boolean teamInvitations, Boolean questReminders, 
                                              Boolean achievementNotifications, 
                                              Boolean friendRequests, Boolean systemNotifications);

    /**
     * Обновляет настройки интерфейса
     */
    UserSettingsDTO updateInterfaceSettings(UUID userId, String theme, String language, 
                                           String timezone, String dateFormat, String timeFormat);

    /**
     * Обновляет игровые настройки
     */
    UserSettingsDTO updateGameSettings(UUID userId, Boolean autoJoinTeams, Boolean showHints, 
                                      Boolean soundEffects, Boolean music, Boolean animations);

    /**
     * Применяет настройки по умолчанию
     */
    UserSettingsDTO resetToDefaults(UUID userId);

    /**
     * Удаляет настройки пользователя
     */
    void deleteUserSettings(UUID userId);

    /**
     * Получает пользователей с публичными профилями
     */
    List<UserSettingsDTO> getUsersWithPublicProfiles();

    /**
     * Получает пользователей с включенными email уведомлениями
     */
    List<UserSettingsDTO> getUsersWithEmailNotifications();

    /**
     * Получает пользователей для рассылки уведомлений определенного типа
     */
    List<UserSettingsDTO> getUsersForNotificationType(String notificationType);

    /**
     * Получает пользователей с определенной темой оформления
     */
    List<UserSettingsDTO> getUsersByTheme(String theme);

    /**
     * Получает пользователей с определенным языком
     */
    List<UserSettingsDTO> getUsersByLanguage(String language);

    /**
     * Получает пользователей с определенным часовым поясом
     */
    List<UserSettingsDTO> getUsersByTimezone(String timezone);

    /**
     * Проверяет существование настроек пользователя
     */
    boolean existsByUserId(UUID userId);

    /**
     * Получает статистику по настройкам
     */
    dn.quest.usermanagement.dto.SettingsStatisticsSummaryDTO getSettingsStatistics();
}