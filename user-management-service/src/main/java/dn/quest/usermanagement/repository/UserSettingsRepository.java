package dn.quest.usermanagement.repository;

import dn.quest.usermanagement.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository для работы с настройками пользователей
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    // Базовые запросы
    Optional<UserSettings> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // Поиск по настройкам приватности
    List<UserSettings> findByProfilePublic(Boolean profilePublic);
    List<UserSettings> findByShowEmail(Boolean showEmail);
    List<UserSettings> findByShowStatistics(Boolean showStatistics);

    // Поиск по настройкам уведомлений
    List<UserSettings> findByEmailNotifications(Boolean emailNotifications);
    List<UserSettings> findByTeamInvitations(Boolean teamInvitations);
    List<UserSettings> findByQuestReminders(Boolean questReminders);
    List<UserSettings> findByAchievementNotifications(Boolean achievementNotifications);
    List<UserSettings> findByFriendRequests(Boolean friendRequests);
    List<UserSettings> findBySystemNotifications(Boolean systemNotifications);

    // Поиск по настройкам интерфейса
    List<UserSettings> findByTheme(String theme);
    List<UserSettings> findByLanguage(String language);
    List<UserSettings> findByTimezone(String timezone);

    // Комплексные запросы для поиска пользователей с определенными настройками
    @Query("SELECT us FROM UserSettings us WHERE " +
           "(:profilePublic IS NULL OR us.profilePublic = :profilePublic) AND " +
           "(:showEmail IS NULL OR us.showEmail = :showEmail) AND " +
           "(:emailNotifications IS NULL OR us.emailNotifications = :emailNotifications) AND " +
           "(:teamInvitations IS NULL OR us.teamInvitations = :teamInvitations)")
    List<UserSettings> findByPrivacyAndNotificationSettings(
            @Param("profilePublic") Boolean profilePublic,
            @Param("showEmail") Boolean showEmail,
            @Param("emailNotifications") Boolean emailNotifications,
            @Param("teamInvitations") Boolean teamInvitations
    );

    // Поиск пользователей для рассылки уведомлений
    @Query("SELECT us FROM UserSettings us WHERE " +
           "us.emailNotifications = true AND " +
           "(:notificationType IS NULL OR " +
           " (:notificationType = 'TEAM_INVITATIONS' AND us.teamInvitations = true) OR " +
           " (:notificationType = 'QUEST_REMINDERS' AND us.questReminders = true) OR " +
           " (:notificationType = 'ACHIEVEMENTS' AND us.achievementNotifications = true) OR " +
           " (:notificationType = 'FRIEND_REQUESTS' AND us.friendRequests = true) OR " +
           " (:notificationType = 'SYSTEM' AND us.systemNotifications = true))")
    List<UserSettings> findUsersForEmailNotifications(@Param("notificationType") String notificationType);

    // Поиск пользователей с публичными профилями
    @Query("SELECT us FROM UserSettings us WHERE us.profilePublic = true AND " +
           "(:showStatistics IS NULL OR us.showStatistics = :showStatistics)")
    List<UserSettings> findUsersWithPublicProfiles(@Param("showStatistics") Boolean showStatistics);

    // Статистические запросы
    @Query("SELECT COUNT(us) FROM UserSettings us WHERE us.profilePublic = :profilePublic")
    long countByProfilePublic(@Param("profilePublic") Boolean profilePublic);

    @Query("SELECT COUNT(us) FROM UserSettings us WHERE us.emailNotifications = :emailNotifications")
    long countByEmailNotifications(@Param("emailNotifications") Boolean emailNotifications);

    @Query("SELECT us.theme, COUNT(us) FROM UserSettings us GROUP BY us.theme")
    List<Object[]> countByTheme();

    @Query("SELECT us.language, COUNT(us) FROM UserSettings us GROUP BY us.language")
    List<Object[]> countByLanguage();

    @Query("SELECT us.timezone, COUNT(us) FROM UserSettings us GROUP BY us.timezone")
    List<Object[]> countByTimezone();

    // Поиск пользователей для маркетинговых кампаний
    @Query("SELECT us FROM UserSettings us WHERE " +
           "us.emailNotifications = true AND " +
           "us.profilePublic = true AND " +
           "us.showStatistics = true")
    List<UserSettings> findUsersForMarketingCampaigns();

    // Поиск пользователей с определенными игровыми настройками
    @Query("SELECT us FROM UserSettings us WHERE " +
           "(:autoJoinTeams IS NULL OR us.autoJoinTeams = :autoJoinTeams) AND " +
           "(:showHints IS NULL OR us.showHints = :showHints) AND " +
           "(:soundEffects IS NULL OR us.soundEffects = :soundEffects)")
    List<UserSettings> findByGameSettings(
            @Param("autoJoinTeams") Boolean autoJoinTeams,
            @Param("showHints") Boolean showHints,
            @Param("soundEffects") Boolean soundEffects
    );

    // Массовые операции для обновления настроек
    @Query("UPDATE UserSettings us SET us.emailNotifications = :enabled WHERE us.userId IN :userIds")
    void updateEmailNotificationsForUsers(@Param("userIds") List<Long> userIds, @Param("enabled") Boolean enabled);

    @Query("UPDATE UserSettings us SET us.theme = :theme WHERE us.userId IN :userIds")
    void updateThemeForUsers(@Param("userIds") List<Long> userIds, @Param("theme") String theme);

    @Query("UPDATE UserSettings us SET us.language = :language WHERE us.userId IN :userIds")
    void updateLanguageForUsers(@Param("userIds") List<Long> userIds, @Param("language") String language);
}