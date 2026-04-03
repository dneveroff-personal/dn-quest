package dn.quest.teammanagement.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign клиент для взаимодействия с Statistics Service
 */
@FeignClient(name = "statistics-service", url = "${app.services.statistics-service.url}")
public interface StatisticsServiceClient {

    /**
     * Отправка статистики команды
     */
    @PostMapping("/api/statistics/teams")
    void sendTeamStatistics(@RequestBody TeamStatisticsDataDTO statistics);

    /**
     * Отправка статистики пользователя
     */
    @PostMapping("/api/statistics/users")
    void sendUserStatistics(@RequestBody UserStatisticsDataDTO statistics);

    /**
     * Отправка статистики приглашений
     */
    @PostMapping("/api/statistics/invitations")
    void sendInvitationStatistics(@RequestBody InvitationStatisticsDataDTO statistics);

    /**
     * Отправка статистики игровых сессий
     */
    @PostMapping("/api/statistics/game-sessions")
    void sendGameSessionStatistics(@RequestBody GameSessionStatisticsDataDTO statistics);

    /**
     * Получение агрегированной статистики команды
     */
    @GetMapping("/api/statistics/teams/{teamId}/aggregated")
    TeamAggregatedStatisticsDTO getTeamAggregatedStatistics(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "period", defaultValue = "30") int period
    );

    /**
     * Получение агрегированной статистики пользователя
     */
    @GetMapping("/api/statistics/users/{userId}/aggregated")
    UserAggregatedStatisticsDTO getUserAggregatedStatistics(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "period", defaultValue = "30") int period
    );

    /**
     * Получение глобальной статистики пользователей
     */
    @GetMapping("/api/statistics/users/global")
    GlobalUserStatisticsDTO getGlobalUserStatistics();

    /**
     * Получение трендов статистики
     */
    @GetMapping("/api/statistics/trends")
    StatisticsTrendsDTO getStatisticsTrends(
            @RequestParam(value = "period", defaultValue = "7") int period,
            @RequestParam(value = "metric") String metric
    );

    /**
     * Получение рейтинга команд
     */
    @GetMapping("/api/statistics/teams/ranking")
    List<TeamRankingDTO> getTeamsRanking(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "sortBy", defaultValue = "score") String sortBy
    );

    /**
     * Получение рейтинга пользователей
     */
    @GetMapping("/api/statistics/users/ranking")
    List<UserRankingDTO> getUsersRanking(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "sortBy", defaultValue = "score") String sortBy
    );

    /**
     * Получение статистики по периодам
     */
    @GetMapping("/api/statistics/teams/{teamId}/history")
    List<TeamStatisticsHistoryDTO> getTeamStatisticsHistory(
            @PathVariable("teamId") UUID teamId,
            @RequestParam("startDate") Instant startDate,
            @RequestParam("endDate") Instant endDate,
            @RequestParam(value = "granularity", defaultValue = "DAILY") String granularity
    );

    /**
     * Получение статистики пользователя по периодам
     */
    @GetMapping("/api/statistics/users/{userId}/history")
    List<UserStatisticsHistoryDTO> getUserStatisticsHistory(
            @PathVariable("userId") UUID userId,
            @RequestParam("startDate") Instant startDate,
            @RequestParam("endDate") Instant endDate,
            @RequestParam(value = "granularity", defaultValue = "DAILY") String granularity
    );

    /**
     * Получение сравнительной статистики команд
     */
    @PostMapping("/api/statistics/teams/compare")
    TeamComparisonDTO compareTeams(@RequestBody List<Long> teamIds);

    /**
     * Получение сравнительной статистики пользователей
     */
    @PostMapping("/api/statistics/users/compare")
    UserComparisonDTO compareUsers(@RequestBody List<Long> userIds);

    /**
     * Получение прогнозов статистики
     */
    @GetMapping("/api/statistics/teams/{teamId}/forecast")
    TeamForecastDTO getTeamForecast(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "period", defaultValue = "30") int period
    );

    /**
     * Получение аномалий в статистике
     */
    @GetMapping("/api/statistics/anomalies")
    List<StatisticsAnomalyDTO> getStatisticsAnomalies(
            @RequestParam(value = "period", defaultValue = "7") int period
    );

    /**
     * Экспорт статистики
     */
    @GetMapping("/api/statistics/export")
    byte[] exportStatistics(
            @RequestParam("type") String type,
            @RequestParam("format") String format,
            @RequestParam(value = "startDate", required = false) Instant startDate,
            @RequestParam(value = "endDate", required = false) Instant endDate
    );

    // Внутренние DTO классы для Statistics Service

    @Setter
    @Getter
    class TeamStatisticsDataDTO {
        private UUID teamId;
        private String teamName;
        private String teamTag;
        private Integer memberCount;
        private Integer activeMemberCount;
        private Long totalInvitationsSent;
        private Long totalInvitationsAccepted;
        private Long totalInvitationsDeclined;
        private Double acceptanceRate;
        private Integer totalGameSessions;
        private Integer completedGameSessions;
        private Double averageGameScore;
        private Integer totalScore;
        private String lastActivityAt;
        private String timestamp;
    }

    @Setter
    @Getter
    class UserStatisticsDataDTO {
        private UUID userId;
        private String username;
        private String email;
        private Integer teamCount;
        private Integer activeTeamCount;
        private Integer captainTeamCount;
        private Integer invitationCount;
        private Integer acceptedInvitationCount;
        private Integer declinedInvitationCount;
        private Double teamParticipationRate;
        private Integer totalGameSessions;
        private Integer completedGameSessions;
        private Double averageGameScore;
        private Integer totalScore;
        private String lastActivityAt;
        private String timestamp;
    }

    @Setter
    @Getter
    class InvitationStatisticsDataDTO {
        private Long invitationId;
        private UUID teamId;
        private String teamName;
        private UUID userId;
        private String username;
        private Long invitedBy;
        private String invitedByUsername;
        private String status;
        private String createdAt;
        private String respondedAt;
        private String responseMessage;
    }

    @Setter
    @Getter
    class GameSessionStatisticsDataDTO {
        private Long sessionId;
        private Long questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private String status;
        private String startTime;
        private String endTime;
        private String duration;
        private Integer score;
        private Integer maxScore;
        private Double completionPercentage;
        private Integer levelsCompleted;
        private Integer totalLevels;
        private Integer participantCount;
        private List<Long> participantIds;
    }

    @Setter
    @Getter
    class TeamAggregatedStatisticsDTO {
        private UUID teamId;
        private String teamName;
        private String period;
        private Integer totalMembers;
        private Integer activeMembers;
        private Long totalInvitations;
        private Double acceptanceRate;
        private Integer totalSessions;
        private Double averageScore;
        private Integer totalScore;
        private String mostActiveDay;
        private String peakActivityHour;
    }

    @Getter
    @Setter
    class UserAggregatedStatisticsDTO {
        // Getters and setters
        private UUID userId;
        private String username;
        private String period;
        private Integer totalTeams;
        private Integer activeTeams;
        private Long totalInvitations;
        private Double acceptanceRate;
        private Integer totalSessions;
        private Double averageScore;
        private Integer totalScore;
        private String mostActiveDay;
        private String peakActivityHour;
    }

    @Setter
    @Getter
    class GlobalUserStatisticsDTO {
        // Getters and setters
        private Long totalUsers;
        private Long activeUsers;
        private Double averageTeamsPerUser;
        private Long totalInvitations;
        private Double averageAcceptanceRate;
        private Long totalSessions;
        private Double averageScore;
        private String mostActiveUser;
    }

    @Setter
    @Getter
    class StatisticsTrendsDTO {
        // Getters and setters
        private String metric;
        private String period;
        private List<TrendDataPoint> dataPoints;
        private Double growthRate;
        private String trend;
    }

    @Setter
    @Getter
    class TrendDataPoint {
        // Getters and setters
        private String timestamp;
        private Double value;
    }

    @Setter
    @Getter
    class TeamRankingDTO {
        // Getters and setters
        private UUID teamId;
        private String teamName;
        private String teamTag;
        private Integer rank;
        private Double score;
        private String metric;
    }

    @Setter
    @Getter
    class UserRankingDTO {
        // Getters and setters
        private UUID userId;
        private String username;
        private Integer rank;
        private Double score;
        private String metric;
    }

    @Setter
    @Getter
    class TeamStatisticsHistoryDTO {
        // Getters and setters
        private String timestamp;
        private Integer memberCount;
        private Integer activeMemberCount;
        private Long totalInvitations;
        private Double acceptanceRate;
        private Integer totalSessions;
        private Double averageScore;
    }

    @Setter
    @Getter
    class UserStatisticsHistoryDTO {
        // Getters and setters
        private String timestamp;
        private Integer teamCount;
        private Integer activeTeamCount;
        private Long totalInvitations;
        private Double acceptanceRate;
        private Integer totalSessions;
        private Double averageScore;
    }

    @Setter
    @Getter
    class TeamComparisonDTO {
        private List<TeamComparisonItemDTO> teams;
        private Map<String, Object> comparisonMetrics;
    }

    @Setter
    @Getter
    class TeamComparisonItemDTO {
        private UUID teamId;
        private String teamName;
        private Map<String, Double> metrics;

    }

    @Setter
    @Getter
    class UserComparisonDTO {
        private List<UserComparisonItemDTO> users;
        private Map<String, Object> comparisonMetrics;

    }

    @Setter
    @Getter
    class UserComparisonItemDTO {
        private UUID userId;
        private String username;
        private Map<String, Double> metrics;
    }

    @Setter
    @Getter
    class TeamForecastDTO {
        private UUID teamId;
        private String teamName;
        private String period;
        private Map<String, List<ForecastDataPoint>> forecasts;
    }

    @Setter
    @Getter
    class ForecastDataPoint {
        private String timestamp;
        private Double predictedValue;
        private Double confidenceInterval;
    }

    @Setter
    @Getter
    class StatisticsAnomalyDTO {
        private String metric;
        private String entityType;
        private Long entityId;
        private String entityName;
        private String timestamp;
        private Double expectedValue;
        private Double actualValue;
        private Double deviationPercentage;
        private String severity;
    }
}
