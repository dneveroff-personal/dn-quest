package dn.quest.statistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность для хранения командной статистики
 */
@Entity
@Table(name = "team_statistics", indexes = {
    @Index(name = "idx_team_statistics_team_id", columnList = "teamId"),
    @Index(name = "idx_team_statistics_date", columnList = "date"),
    @Index(name = "idx_team_statistics_captain_id", columnList = "captainId"),
    @Index(name = "idx_team_statistics_team_date", columnList = "teamId, date"),
    @Index(name = "idx_team_statistics_captain_date", columnList = "captainId, date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID команды
     */
    @Column(name = "team_id", nullable = false)
    private Long teamId;

    /**
     * Название команды
     */
    @Column(name = "team_name")
    private String teamName;

    /**
     * ID капитана команды
     */
    @Column(name = "captain_id")
    private Long captainId;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Количество созданий команд (для агрегированных записей)
     */
    @Column(name = "creations")
    private Integer creations;

    /**
     * Количество обновлений команды
     */
    @Column(name = "updates")
    private Integer updates;

    /**
     * Количество удалений команды
     */
    @Column(name = "deletions")
    private Integer deletions;

    /**
     * Текущее количество участников
     */
    @Column(name = "current_members_count")
    private Integer currentMembersCount;

    /**
     * Максимальное количество участников
     */
    @Column(name = "max_members")
    private Integer maxMembers;

    /**
     * Количество добавлений участников
     */
    @Column(name = "member_additions")
    private Integer memberAdditions;

    /**
     * Количество удалений участников
     */
    @Column(name = "member_removals")
    private Integer memberRemovals;

    /**
     * Количество уникальных участников за все время
     */
    @Column(name = "total_unique_members")
    private Integer totalUniqueMembers;

    /**
     * Количество сыгранных квестов
     */
    @Column(name = "played_quests")
    private Integer playedQuests;

    /**
     * Количество завершенных квестов
     */
    @Column(name = "completed_quests")
    private Integer completedQuests;

    /**
     * Количество побед в квестах
     */
    @Column(name = "quest_wins")
    private Integer questWins;

    /**
     * Общее время в игре (в минутах)
     */
    @Column(name = "total_game_time_minutes")
    private Long totalGameTimeMinutes;

    /**
     * Среднее время прохождения квеста (в минутах)
     */
    @Column(name = "avg_quest_completion_time_minutes")
    private Double avgQuestCompletionTimeMinutes;

    /**
     * Текущий рейтинг команды
     */
    @Column(name = "current_rating")
    private Double currentRating;

    /**
     * Изменение рейтинга за день
     */
    @Column(name = "rating_change")
    private Double ratingChange;

    /**
     * Количество успешных отправок кода
     */
    @Column(name = "successful_code_submissions")
    private Integer successfulCodeSubmissions;

    /**
     * Количество неудачных отправок кода
     */
    @Column(name = "failed_code_submissions")
    private Integer failedCodeSubmissions;

    /**
     * Количество завершенных уровней
     */
    @Column(name = "completed_levels")
    private Integer completedLevels;

    /**
     * Среднее время завершения уровня (в секундах)
     */
    @Column(name = "avg_level_completion_time_seconds")
    private Double avgLevelCompletionTimeSeconds;

    /**
     * Тип команды (public, private, invite_only)
     */
    @Column(name = "team_type")
    private String teamType;

    /**
     * Статус команды (active, inactive, archived)
     */
    @Column(name = "status")
    private String status;

    /**
     * Теги команды (JSON массив)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Количество просмотров страницы команды
     */
    @Column(name = "profile_views")
    private Integer profileViews;

    /**
     * Количество уникальных просмотров
     */
    @Column(name = "unique_profile_views")
    private Integer uniqueProfileViews;

    /**
     * Количество приглашений отправлено
     */
    @Column(name = "invitations_sent")
    private Integer invitationsSent;

    /**
     * Количество приглашений принято
     */
    @Column(name = "invitations_accepted")
    private Integer invitationsAccepted;

    /**
     * Количество приглашений отклонено
     */
    @Column(name = "invitations_declined")
    private Integer invitationsDeclined;

    /**
     * Последняя активность команды
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * IP адрес последней активности
     */
    @Column(name = "last_ip")
    private String lastIp;

    /**
     * User Agent последней активности
     */
    @Column(name = "last_user_agent")
    private String lastUserAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}