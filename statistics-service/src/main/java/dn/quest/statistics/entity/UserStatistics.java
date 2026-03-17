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
 * Сущность для хранения статистики пользователей
 */
@Entity
@Table(name = "user_statistics", indexes = {
    @Index(name = "idx_user_statistics_user_id", columnList = "userId"),
    @Index(name = "idx_user_statistics_date", columnList = "date"),
    @Index(name = "idx_user_statistics_user_date", columnList = "userId, date"),
    @Index(name = "idx_user_statistics_last_active", columnList = "lastActiveAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID пользователя
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Количество регистраций (для агрегированных записей)
     */
    @Column(name = "registrations")
    private Integer registrations;

    /**
     * Количество логинов
     */
    @Column(name = "logins")
    private Integer logins;

    /**
     * Количество игровых сессий
     */
    @Column(name = "game_sessions")
    private Integer gameSessions;

    /**
     * Количество завершенных квестов
     */
    @Column(name = "completed_quests")
    private Integer completedQuests;

    /**
     * Количество созданных квестов
     */
    @Column(name = "created_quests")
    private Integer createdQuests;

    /**
     * Количество созданных команд
     */
    @Column(name = "created_teams")
    private Integer createdTeams;

    /**
     * Количество участий в командах
     */
    @Column(name = "team_memberships")
    private Integer teamMemberships;

    /**
     * Общее время в игре (в минутах)
     */
    @Column(name = "total_game_time_minutes")
    private Long totalGameTimeMinutes;

    /**
     * Количество загруженных файлов
     */
    @Column(name = "uploaded_files")
    private Integer uploadedFiles;

    /**
     * Общий размер загруженных файлов (в байтах)
     */
    @Column(name = "total_file_size_bytes")
    private Long totalFileSizeBytes;

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
     * Текущий рейтинг пользователя
     */
    @Column(name = "current_rating")
    private Double currentRating;

    /**
     * Изменение рейтинга за день
     */
    @Column(name = "rating_change")
    private Double ratingChange;

    /**
     * Последняя активность
     */
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

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