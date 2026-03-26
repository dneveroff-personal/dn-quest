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
 * Сущность для хранения агрегированной ежедневной статистики по всей платформе
 */
@Entity
@Table(name = "daily_aggregated_statistics", indexes = {
    @Index(name = "idx_daily_aggregated_date", columnList = "date"),
    @Index(name = "idx_daily_aggregated_type", columnList = "aggregationType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAggregatedStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    /**
     * Тип агрегации (daily, weekly, monthly)
     */
    @Column(name = "aggregation_type", nullable = false)
    private String aggregationType;

    // Пользовательская статистика
    /**
     * Общее количество пользователей
     */
    @Column(name = "total_users")
    private Long totalUsers;

    /**
     * Количество новых регистраций
     */
    @Column(name = "new_registrations")
    private Integer newRegistrations;

    /**
     * Количество активных пользователей (DAU)
     */
    @Column(name = "active_users")
    private Long activeUsers;

    /**
     * Количество уникальных пользователей за месяц (MAU)
     */
    @Column(name = "monthly_active_users")
    private Integer monthlyActiveUsers;

    /**
     * Коэффициент удержания (retention rate)
     */
    @Column(name = "retention_rate")
    private Double retentionRate;

    // Статистика квестов
    /**
     * Общее количество квестов
     */
    @Column(name = "total_quests")
    private Long totalQuests;

    /**
     * Количество новых квестов
     */
    @Column(name = "new_quests")
    private Integer newQuests;

    /**
     * Количество опубликованных квестов
     */
    @Column(name = "published_quests")
    private Integer publishedQuests;

    /**
     * Количество завершенных квестов
     */
    @Column(name = "completed_quests")
    private Long completedQuests;

    /**
     * Среднее время прохождения квеста (в минутах)
     */
    @Column(name = "avg_quest_completion_time")
    private Double avgQuestCompletionTime;

    // Игровая статистика
    /**
     * Общее количество игровых сессий
     */
    @Column(name = "total_game_sessions")
    private Long totalGameSessions;

    /**
     * Количество завершенных игровых сессий
     */
    @Column(name = "completed_game_sessions")
    private Long completedGameSessions;

    /**
     * Общее время в игре (в минутах)
     */
    @Column(name = "total_game_time")
    private Long totalGameTime;

    /**
     * Среднее время игровой сессии (в минутах)
     */
    @Column(name = "avg_session_time")
    private Double avgSessionTime;

    /**
     * Количество отправок кода
     */
    @Column(name = "code_submissions")
    private Integer codeSubmissions;

    /**
     * Количество успешных отправок кода
     */
    @Column(name = "successful_code_submissions")
    private Integer successfulCodeSubmissions;

    /**
     * Коэффициент успешности отправок кода
     */
    @Column(name = "code_success_rate")
    private Double codeSuccessRate;

    // Командная статистика
    /**
     * Общее количество команд
     */
    @Column(name = "total_teams")
    private Long totalTeams;

    /**
     * Количество новых команд
     */
    @Column(name = "new_teams")
    private Integer newTeams;

    /**
     * Количество активных команд
     */
    @Column(name = "active_teams")
    private Long activeTeams;

    /**
     * Среднее количество участников в команде
     */
    @Column(name = "avg_team_size")
    private Double avgTeamSize;

    // Системная статистика
    /**
     * Пиковое количество одновременных пользователей
     */
    @Column(name = "peak_concurrent_users")
    private Integer peakConcurrentUsers;

    /**
     * Среднее время ответа системы (в миллисекундах)
     */
    @Column(name = "avg_response_time")
    private Double avgResponseTime;

    /**
     * Количество ошибок системы
     */
    @Column(name = "system_errors")
    private Integer systemErrors;

    /**
     * Время работы системы (в процентах)
     */
    @Column(name = "uptime_percentage")
    private Double uptimePercentage;

    // Бизнес метрики
    /**
     * Коэффициент конверсии (регистрации -> активные пользователи)
     */
    @Column(name = "conversion_rate")
    private Double conversionRate;

    /**
     * Средний доход на пользователя (ARPU)
     */
    @Column(name = "avg_revenue_per_user")
    private Double avgRevenuePerUser;

    /**
     * Пожизненная ценность клиента (LTV)
     */
    @Column(name = "customer_lifetime_value")
    private Double customerLifetimeValue;

    /**
     * Стоимость привлечения клиента (CAC)
     */
    @Column(name = "customer_acquisition_cost")
    private Double customerAcquisitionCost;

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