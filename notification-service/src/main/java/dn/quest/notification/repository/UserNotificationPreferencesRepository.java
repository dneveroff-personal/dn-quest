package dn.quest.notification.repository;

import dn.quest.notification.entity.UserNotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с пользовательскими предпочтениями уведомлений
 */
@Repository
public interface UserNotificationPreferencesRepository extends JpaRepository<UserNotificationPreferences, Long> {

    /**
     * Найти предпочтения пользователя по ID
     */
    Optional<UserNotificationPreferences> findByUserId(UUID userId);

    /**
     * Проверить существование предпочтений для пользователя
     */
    boolean existsByUserId(UUID userId);

    /**
     * Найти пользователей с включенными email уведомлениями
     */
    List<UserNotificationPreferences> findByEmailEnabledTrue();

    /**
     * Найти пользователей с включенными push уведомлениями
     */
    List<UserNotificationPreferences> findByPushEnabledTrue();

    /**
     * Найти пользователей с включенными telegram уведомлениями
     */
    List<UserNotificationPreferences> findByTelegramEnabledTrue();

    /**
     * Найти пользователей с включенными SMS уведомлениями
     */
    List<UserNotificationPreferences> findBySmsEnabledTrue();

    /**
     * Найти пользователей с включенными уведомлениями о квестах
     */
    List<UserNotificationPreferences> findByQuestEnabledTrue();

    /**
     * Найти пользователей с включенными игровыми уведомлениями
     */
    List<UserNotificationPreferences> findByGameEnabledTrue();

    /**
     * Найти пользователей с включенными командными уведомлениями
     */
    List<UserNotificationPreferences> findByTeamEnabledTrue();

    /**
     * Найти пользователей с включенными системными уведомлениями
     */
    List<UserNotificationPreferences> findBySystemEnabledTrue();

    /**
     * Найти пользователей с включенными уведомлениями о безопасности
     */
    List<UserNotificationPreferences> findBySecurityEnabledTrue();

    /**
     * Найти пользователей с включенными маркетинговыми уведомлениями
     */
    List<UserNotificationPreferences> findByMarketingEnabledTrue();

    /**
     * Найти пользователей с включенными напоминаниями
     */
    List<UserNotificationPreferences> findByReminderEnabledTrue();

    /**
     * Найти пользователей с отключенным Do Not Disturb режимом
     */
    @Query("SELECT p FROM UserNotificationPreferences p WHERE p.doNotDisturbEnabled = false OR p.doNotDisturbEnabled IS NULL")
    List<UserNotificationPreferences> findUsersWithoutDoNotDisturb();

    /**
     * Обновить FCM token пользователя
     */
    @Modifying
    @Query("UPDATE UserNotificationPreferences p SET p.fcmToken = :fcmToken WHERE p.userId = :userId")
    int updateFcmToken(@Param("userId") UUID userId, @Param("fcmToken") String fcmToken);

    /**
     * Обновить Telegram chat ID пользователя
     */
    @Modifying
    @Query("UPDATE UserNotificationPreferences p SET p.telegramChatId = :telegramChatId WHERE p.userId = :userId")
    int updateTelegramChatId(@Param("userId") UUID userId, @Param("telegramChatId") String telegramChatId);

    /**
     * Обновить email пользователя
     */
    @Modifying
    @Query("UPDATE UserNotificationPreferences p SET p.email = :email WHERE p.userId = :userId")
    int updateEmail(@Param("userId") UUID userId, @Param("email") String email);

    /**
     * Обновить телефон пользователя
     */
    @Modifying
    @Query("UPDATE UserNotificationPreferences p SET p.phone = :phone WHERE p.userId = :userId")
    int updatePhone(@Param("userId") UUID userId, @Param("phone") String phone);

    /**
     * Подсчитать количество пользователей с включенными уведомлениями по типу
     */
    @Query("SELECT COUNT(p) FROM UserNotificationPreferences p WHERE p.emailEnabled = true")
    long countUsersWithEmailEnabled();

    @Query("SELECT COUNT(p) FROM UserNotificationPreferences p WHERE p.pushEnabled = true")
    long countUsersWithPushEnabled();

    @Query("SELECT COUNT(p) FROM UserNotificationPreferences p WHERE p.telegramEnabled = true")
    long countUsersWithTelegramEnabled();

    @Query("SELECT COUNT(p) FROM UserNotificationPreferences p WHERE p.smsEnabled = true")
    long countUsersWithSmsEnabled();

    /**
     * Найти пользователей для массовой рассылки
     */
    @Query("SELECT p FROM UserNotificationPreferences p WHERE " +
           "(:emailEnabled IS NULL OR p.emailEnabled = :emailEnabled) AND " +
           "(:pushEnabled IS NULL OR p.pushEnabled = :pushEnabled) AND " +
           "(:telegramEnabled IS NULL OR p.telegramEnabled = :telegramEnabled) AND " +
           "(:smsEnabled IS NULL OR p.smsEnabled = :smsEnabled)")
    List<UserNotificationPreferences> findUsersForBulkNotification(
            @Param("emailEnabled") Boolean emailEnabled,
            @Param("pushEnabled") Boolean pushEnabled,
            @Param("telegramEnabled") Boolean telegramEnabled,
            @Param("smsEnabled") Boolean smsEnabled);
}