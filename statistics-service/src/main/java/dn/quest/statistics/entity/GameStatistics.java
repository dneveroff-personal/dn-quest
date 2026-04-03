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
import java.util.UUID;

/**
 * Сущность для хранения игровой статистики
 */
@Entity
@Table(name = "game_statistics", indexes = {
    @Index(name = "idx_game_statistics_session_id", columnList = "sessionId"),
    @Index(name = "idx_game_statistics_date", columnList = "date"),
    @Index(name = "idx_game_statistics_user_id", columnList = "userId"),
    @Index(name = "idx_game_statistics_quest_id", columnList = "questId"),
    @Index(name = "idx_game_statistics_team_id", columnList = "teamId"),
    @Index(name = "idx_game_statistics_user_date", columnList = "userId, date"),
    @Index(name = "idx_game_statistics_quest_date", columnList = "questId, date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID игровой сессии
     */
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    /**
     * ID пользователя
     */
    @Column(name = "user_id")
    private UUID userId;

    /**
     * ID команды (если командная игра)
     */
    @Column(name = "team_id")
    private UUID teamId;

    /**
     * ID квеста
     */
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Тип сессии (individual, team)
     */
    @Column(name = "session_type")
    private String sessionType;

    /**
     * Время начала сессии
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * Время окончания сессии
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Общая длительность сессии (в минутах)
     */
    @Column(name = "duration_minutes")
    private Long durationMinutes;

    /**
     * Текущий уровень
     */
    @Column(name = "current_level")
    private Integer currentLevel;

    /**
     * Общее количество уровней
     */
    @Column(name = "total_levels")
    private Integer totalLevels;

    /**
     * Количество завершенных уровней
     */
    @Column(name = "completed_levels")
    private Integer completedLevels;

    /**
     * Статус сессии (started, finished, abandoned, paused)
     */
    @Column(name = "status")
    private String status;

    /**
     * Завершена ли сессия успешно
     */
    @Column(name = "is_completed")
    private Boolean isCompleted;

    /**
     * Количество отправок кода
     */
    @Column(name = "code_submissions")
    private Integer codeSubmissions;

    /**
     * Количество успешных отправок кода
     */
    @Column(name = "successful_submissions")
    private Integer successfulSubmissions;

    /**
     * Количество неудачных отправок кода
     */
    @Column(name = "failed_submissions")
    private Integer failedSubmissions;

    /**
     * Общее количество попыток
     */
    @Column(name = "total_attempts")
    private Integer totalAttempts;

    /**
     * Среднее время на уровень (в минутах)
     */
    @Column(name = "avg_level_time_minutes")
    private Double avgLevelTimeMinutes;

    /**
     * Самое быстрое время завершения уровня (в секундах)
     */
    @Column(name = "fastest_level_completion_seconds")
    private Long fastestLevelCompletionSeconds;

    /**
     * Самое медленное время завершения уровня (в секундах)
     */
    @Column(name = "slowest_level_completion_seconds")
    private Long slowestLevelCompletionSeconds;

    /**
     * Количество подсказок использовано
     */
    @Column(name = "hints_used")
    private Integer hintsUsed;

    /**
     * Количество бонусов получено
     */
    @Column(name = "bonuses_earned")
    private Integer bonusesEarned;

    /**
     * Набранные очки
     */
    @Column(name = "score")
    private Integer score;

    /**
     * Максимально возможные очки
     */
    @Column(name = "max_score")
    private Integer maxScore;

    /**
     * Процент выполнения (score / max_score * 100)
     */
    @Column(name = "completion_percentage")
    private Double completionPercentage;

    /**
     * IP адрес начала сессии
     */
    @Column(name = "start_ip")
    private String startIp;

    /**
     * User Agent при начале сессии
     */
    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Устройство (mobile, desktop, tablet)
     */
    @Column(name = "device_type")
    private String deviceType;

    /**
     * Браузер
     */
    @Column(name = "browser")
    private String browser;

    /**
     * Операционная система
     */
    @Column(name = "operating_system")
    private String operatingSystem;

    /**
     * Дополнительные метаданные (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}