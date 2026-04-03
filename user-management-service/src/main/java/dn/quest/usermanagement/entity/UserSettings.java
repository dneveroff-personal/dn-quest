package dn.quest.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность настроек пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_settings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_settings_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_user_settings_user_id", columnList = "user_id")
        })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // Настройки приватности
    @Column(name = "profile_public", nullable = false)
    @Builder.Default
    private Boolean profilePublic = true;

    @Column(name = "show_email", nullable = false)
    @Builder.Default
    private Boolean showEmail = false;

    @Column(name = "show_real_name", nullable = false)
    @Builder.Default
    private Boolean showRealName = false;

    @Column(name = "show_location", nullable = false)
    @Builder.Default
    private Boolean showLocation = true;

    @Column(name = "show_website", nullable = false)
    @Builder.Default
    private Boolean showWebsite = true;

    @Column(name = "show_statistics", nullable = false)
    @Builder.Default
    private Boolean showStatistics = true;

    // Настройки уведомлений
    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "team_invitations", nullable = false)
    @Builder.Default
    private Boolean teamInvitations = true;

    @Column(name = "quest_reminders", nullable = false)
    @Builder.Default
    private Boolean questReminders = true;

    @Column(name = "achievement_notifications", nullable = false)
    @Builder.Default
    private Boolean achievementNotifications = true;

    @Column(name = "friend_requests", nullable = false)
    @Builder.Default
    private Boolean friendRequests = true;

    @Column(name = "system_notifications", nullable = false)
    @Builder.Default
    private Boolean systemNotifications = true;

    // Настройки интерфейса
    @Column(name = "theme", length = 20)
    @Builder.Default
    private String theme = "light";

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "ru";

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "date_format", length = 20)
    @Builder.Default
    private String dateFormat = "dd.MM.yyyy";

    @Column(name = "time_format", length = 10)
    @Builder.Default
    private String timeFormat = "24h";

    // Настройки игры
    @Column(name = "auto_join_teams", nullable = false)
    @Builder.Default
    private Boolean autoJoinTeams = false;

    @Column(name = "show_hints", nullable = false)
    @Builder.Default
    private Boolean showHints = true;

    @Column(name = "sound_effects", nullable = false)
    @Builder.Default
    private Boolean soundEffects = true;

    @Column(name = "music", nullable = false)
    @Builder.Default
    private Boolean music = false;

    @Column(name = "animations", nullable = false)
    @Builder.Default
    private Boolean animations = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Применяет настройки по умолчанию
     */
    public void applyDefaults() {
        this.profilePublic = true;
        this.showEmail = false;
        this.showRealName = false;
        this.showLocation = true;
        this.showWebsite = true;
        this.showStatistics = true;
        
        this.emailNotifications = true;
        this.teamInvitations = true;
        this.questReminders = true;
        this.achievementNotifications = true;
        this.friendRequests = true;
        this.systemNotifications = true;
        
        this.theme = "light";
        this.language = "ru";
        this.timezone = "UTC";
        this.dateFormat = "dd.MM.yyyy";
        this.timeFormat = "24h";
        
        this.autoJoinTeams = false;
        this.showHints = true;
        this.soundEffects = true;
        this.music = false;
        this.animations = true;
    }
}