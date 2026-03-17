package dn.quest.usermanagement.service;

import dn.quest.usermanagement.dto.UpdateSettingsRequestDTO;
import dn.quest.usermanagement.dto.UserSettingsDTO;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления настройками пользователей
 */
public interface UserSettingsService {

    /**
     * Создает настройки пользователя по умолчанию
     */
    UserSettingsDTO createUserSettings(Long userId);

    /**
     * Получает настройки пользователя по ID пользователя
     */
    Optional<UserSettingsDTO> getUserSettingsByUserId(Long userId);

    /**
     * Получает настройки пользователя по ID настроек
     */
    Optional<UserSettingsDTO> getUserSettingsById(Long id);

    /**
     * Обновляет настройки пользователя
     */
    UserSettingsDTO updateUserSettings(Long userId, UpdateSettingsRequestDTO request);

    /**
     * Обновляет настройки приватности
     */
    UserSettingsDTO updatePrivacySettings(Long userId, Boolean profilePublic, Boolean showEmail, 
                                         Boolean showRealName, Boolean showLocation, 
                                         Boolean showWebsite, Boolean showStatistics);

    /**
     * Обновляет настройки уведомлений
     */
    UserSettingsDTO updateNotificationSettings(Long userId, Boolean emailNotifications, 
                                              Boolean teamInvitations, Boolean questReminders, 
                                              Boolean achievementNotifications, 
                                              Boolean friendRequests, Boolean systemNotifications);

    /**
     * Обновляет настройки интерфейса
     */
    UserSettingsDTO updateInterfaceSettings(Long userId, String theme, String language, 
                                           String timezone, String dateFormat, String timeFormat);

    /**
     * Обновляет игровые настройки
     */
    UserSettingsDTO updateGameSettings(Long userId, Boolean autoJoinTeams, Boolean showHints, 
                                      Boolean soundEffects, Boolean music, Boolean animations);

    /**
     * Применяет настройки по умолчанию
     */
    UserSettingsDTO resetToDefaults(Long userId);

    /**
     * Удаляет настройки пользователя
     */
    void deleteUserSettings(Long userId);

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
    boolean existsByUserId(Long userId);

    /**
     * Получает статистику по настройкам
     */
    SettingsStatisticsSummary getSettingsStatistics();
}

/**
 * Класс для сводной статистики настроек
 */
record SettingsStatisticsSummary(
    long totalUsers,
    long publicProfiles,
    long emailNotificationsEnabled,
    java.util.Map<String, Long> themes,
    java.util.Map<String, Long> languages,
    java.util.Map<String, Long> timezones
) {}