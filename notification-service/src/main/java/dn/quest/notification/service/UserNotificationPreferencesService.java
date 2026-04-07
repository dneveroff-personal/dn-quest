package dn.quest.notification.service;

import dn.quest.notification.entity.UserNotificationPreferences;
import dn.quest.notification.enums.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления пользовательскими предпочтениями уведомлений
 */
public interface UserNotificationPreferencesService {

    /**
     * Создать предпочтения для пользователя
     */
    UserNotificationPreferences createPreferences(UUID userId);

    /**
     * Получить предпочтения пользователя
     */
    Optional<UserNotificationPreferences> getPreferences(UUID userId);

    /**
     * Обновить предпочтения пользователя
     */
    UserNotificationPreferences updatePreferences(UUID userId, UserNotificationPreferences preferences);

    /**
     * Обновить конкретные настройки
     */
    UserNotificationPreferences updatePreferences(UUID userId, Map<String, Object> updates);

    /**
     * Удалить предпочтения пользователя
     */
    void deletePreferences(UUID userId);

    /**
     * Проверить, включен ли тип уведомлений для пользователя
     */
    boolean isNotificationTypeEnabled(UUID userId, NotificationType type);

    /**
     * Проверить, включена ли категория уведомлений для пользователя
     */
    boolean isNotificationCategoryEnabled(UUID userId, String category);

    /**
     * Проверить, находится ли пользователь в режиме Do Not Disturb
     */
    boolean isDoNotDisturbActive(UUID userId);

    /**
     * Обновить FCM token пользователя
     */
    void updateFcmToken(UUID userId, String fcmToken);

    /**
     * Обновить Telegram chat ID пользователя
     */
    void updateTelegramChatId(UUID userId, String telegramChatId);

    /**
     * Обновить email пользователя
     */
    void updateEmail(UUID userId, String email);

    /**
     * Обновить телефон пользователя
     */
    void updatePhone(UUID userId, String phone);

    /**
     * Включить/выключить тип уведомлений
     */
    UserNotificationPreferences toggleNotificationType(UUID userId, NotificationType type, boolean enabled);

    /**
     * Включить/выключить категорию уведомлений
     */
    UserNotificationPreferences toggleNotificationCategory(UUID userId, String category, boolean enabled);

    /**
     * Установить Do Not Disturb режим
     */
    UserNotificationPreferences setDoNotDisturb(UUID userId, boolean enabled, Integer startHour, Integer endHour);

    /**
     * Установить лимиты уведомлений
     */
    UserNotificationPreferences setNotificationLimits(UUID userId, Integer perHour, Integer perDay);

    /**
     * Установить предпочитаемый язык
     */
    UserNotificationPreferences setPreferredLanguage(UUID userId, String language);

    /**
     * Установить часовой пояс
     */
    UserNotificationPreferences setTimeZone(UUID userId, String timeZone);

    /**
     * Получить пользователей для массовой рассылки
     */
    List<UserNotificationPreferences> getUsersForBulkNotification(Boolean emailEnabled, 
                                                                  Boolean pushEnabled, 
                                                                  Boolean telegramEnabled, 
                                                                  Boolean smsEnabled);

    /**
     * Получить статистику по предпочтениям
     */
    Map<String, Object> getPreferencesStatistics();

    /**
     * Сбросить настройки пользователя к значениям по умолчанию
     */
    UserNotificationPreferences resetToDefaults(UUID userId);

    /**
     * Экспортировать предпочтения пользователя
     */
    Map<String, Object> exportPreferences(UUID userId);

    /**
     * Импортировать предпочтения пользователя
     */
    UserNotificationPreferences importPreferences(UUID userId, Map<String, Object> data);

    /**
     * Проверить лимиты уведомлений для пользователя
     */
    boolean checkNotificationLimits(UUID userId);

    /**
     * Получить доступные каналы для пользователя
     */
    List<NotificationType> getAvailableChannels(UUID userId);
}