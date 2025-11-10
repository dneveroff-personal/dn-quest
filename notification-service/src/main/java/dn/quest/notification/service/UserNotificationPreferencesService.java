package dn.quest.notification.service;

import dn.quest.notification.entity.UserNotificationPreferences;
import dn.quest.notification.enums.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления пользовательскими предпочтениями уведомлений
 */
public interface UserNotificationPreferencesService {

    /**
     * Создать предпочтения для пользователя
     */
    UserNotificationPreferences createPreferences(Long userId);

    /**
     * Получить предпочтения пользователя
     */
    Optional<UserNotificationPreferences> getPreferences(Long userId);

    /**
     * Обновить предпочтения пользователя
     */
    UserNotificationPreferences updatePreferences(Long userId, UserNotificationPreferences preferences);

    /**
     * Обновить конкретные настройки
     */
    UserNotificationPreferences updatePreferences(Long userId, Map<String, Object> updates);

    /**
     * Удалить предпочтения пользователя
     */
    void deletePreferences(Long userId);

    /**
     * Проверить, включен ли тип уведомлений для пользователя
     */
    boolean isNotificationTypeEnabled(Long userId, NotificationType type);

    /**
     * Проверить, включена ли категория уведомлений для пользователя
     */
    boolean isNotificationCategoryEnabled(Long userId, String category);

    /**
     * Проверить, находится ли пользователь в режиме Do Not Disturb
     */
    boolean isDoNotDisturbActive(Long userId);

    /**
     * Обновить FCM token пользователя
     */
    void updateFcmToken(Long userId, String fcmToken);

    /**
     * Обновить Telegram chat ID пользователя
     */
    void updateTelegramChatId(Long userId, String telegramChatId);

    /**
     * Обновить email пользователя
     */
    void updateEmail(Long userId, String email);

    /**
     * Обновить телефон пользователя
     */
    void updatePhone(Long userId, String phone);

    /**
     * Включить/выключить тип уведомлений
     */
    UserNotificationPreferences toggleNotificationType(Long userId, NotificationType type, boolean enabled);

    /**
     * Включить/выключить категорию уведомлений
     */
    UserNotificationPreferences toggleNotificationCategory(Long userId, String category, boolean enabled);

    /**
     * Установить Do Not Disturb режим
     */
    UserNotificationPreferences setDoNotDisturb(Long userId, boolean enabled, Integer startHour, Integer endHour);

    /**
     * Установить лимиты уведомлений
     */
    UserNotificationPreferences setNotificationLimits(Long userId, Integer perHour, Integer perDay);

    /**
     * Установить предпочитаемый язык
     */
    UserNotificationPreferences setPreferredLanguage(Long userId, String language);

    /**
     * Установить часовой пояс
     */
    UserNotificationPreferences setTimeZone(Long userId, String timeZone);

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
    UserNotificationPreferences resetToDefaults(Long userId);

    /**
     * Экспортировать предпочтения пользователя
     */
    Map<String, Object> exportPreferences(Long userId);

    /**
     * Импортировать предпочтения пользователя
     */
    UserNotificationPreferences importPreferences(Long userId, Map<String, Object> data);

    /**
     * Проверить лимиты уведомлений для пользователя
     */
    boolean checkNotificationLimits(Long userId);

    /**
     * Получить доступные каналы для пользователя
     */
    List<NotificationType> getAvailableChannels(Long userId);
}