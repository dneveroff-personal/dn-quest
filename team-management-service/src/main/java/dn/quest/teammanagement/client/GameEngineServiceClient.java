package dn.quest.teammanagement.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign клиент для взаимодействия с Game Engine Service
 */
@FeignClient(name = "game-engine-service", url = "${app.services.game-engine-service.url}")
public interface GameEngineServiceClient {

    /**
     * Получение информации о игровой сессии по ID
     */
    @GetMapping("/api/game-sessions/{id}")
    GameSessionDTO getGameSessionById(@PathVariable("id") UUID sessionId);

    /**
     * Получение списка игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}")
    List<GameSessionDTO> getGameSessionsByTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение списка игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}")
    List<GameSessionDTO> getGameSessionsByUser(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение активных игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/active")
    List<GameSessionDTO> getActiveGameSessionsByTeam(@PathVariable("teamId") UUID teamId);

    /**
     * Получение активных игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/active")
    List<GameSessionDTO> getActiveGameSessionsByUser(@PathVariable("userId") UUID userId);

    /**
     * Получение завершенных игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/completed")
    List<GameSessionDTO> getCompletedGameSessionsByTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение статистики игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/statistics")
    TeamGameStatisticsDTO getTeamGameStatistics(@PathVariable("teamId") UUID teamId);

    /**
     * Получение статистики игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/statistics")
    UserGameStatisticsDTO getUserGameStatistics(@PathVariable("userId") UUID userId);

    /**
     * Получение количества игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/count")
    long getTeamGameSessionsCount(@PathVariable("teamId") UUID teamId);

    /**
     * Получение количества активных игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/active/count")
    long getTeamActiveGameSessionsCount(@PathVariable("teamId") UUID teamId);

    /**
     * Получение количества игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/count")
    long getUserGameSessionsCount(@PathVariable("userId") UUID userId);

    /**
     * Получение количества активных игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/active/count")
    long getUserActiveGameSessionsCount(@PathVariable("userId") UUID userId);

    /**
     * Проверка, участвует ли команда в активной игровой сессии
     */
    @GetMapping("/api/game-sessions/team/{teamId}/has-active")
    boolean teamHasActiveGameSession(@PathVariable("teamId") UUID teamId);

    /**
     * Проверка, участвует ли пользователь в активной игровой сессии
     */
    @GetMapping("/api/game-sessions/user/{userId}/has-active")
    boolean userHasActiveGameSession(@PathVariable("userId") UUID userId);

    /**
     * Получение информации о квесте по ID
     */
    @GetMapping("/api/quests/{id}")
    QuestDTO getQuestById(@PathVariable("id") UUID questId);

    /**
     * Получение списка доступных квестов для команды
     */
    @GetMapping("/api/quests/available/team/{teamId}")
    List<QuestDTO> getAvailableQuestsForTeam(@PathVariable("teamId") UUID teamId);

    /**
     * Получение списка завершенных квестов для команды
     */
    @GetMapping("/api/quests/completed/team/{teamId}")
    List<QuestDTO> getCompletedQuestsForTeam(@PathVariable("teamId") UUID teamId);

    /**
     * Получение достижений команды
     */
    @GetMapping("/api/achievements/team/{teamId}")
    List<TeamAchievementDTO> getTeamAchievements(@PathVariable("teamId") UUID teamId);

    /**
     * Получение достижений пользователя
     */
    @GetMapping("/api/achievements/user/{userId}")
    List<UserAchievementDTO> getUserAchievements(@PathVariable("userId") UUID userId);

    /**
     * Получение рейтинга команд
     */
    @GetMapping("/api/leaderboard/teams")
    List<TeamLeaderboardDTO> getTeamLeaderboard(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение рейтинга пользователей
     */
    @GetMapping("/api/leaderboard/users")
    List<UserLeaderboardDTO> getUserLeaderboard(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение рейтинга команд по категориям
     */
    @GetMapping("/api/leaderboard/teams/category/{category}")
    List<TeamLeaderboardDTO> getTeamLeaderboardByCategory(
            @PathVariable("category") String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Создание игровой сессии для команды
     */
    @PostMapping("/api/game-sessions")
    GameSessionDTO createGameSession(@RequestBody CreateGameSessionRequest request);

    /**
     * Завершение игровой сессии
     */
    @PutMapping("/api/game-sessions/{id}/finish")
    GameSessionDTO finishGameSession(
            @PathVariable("id") UUID sessionId,
            @RequestBody FinishGameSessionRequest request
    );

    /**
     * Присоединение участника к игровой сессии
     */
    @PostMapping("/api/game-sessions/{id}/participants")
    GameSessionDTO addParticipantToGameSession(
            @PathVariable("id") UUID sessionId,
            @RequestBody AddParticipantRequest request
    );

    /**
     * Удаление участника из игровой сессии
     */
    @DeleteMapping("/api/game-sessions/{id}/participants/{userId}")
    GameSessionDTO removeParticipantFromGameSession(
            @PathVariable("id") UUID sessionId,
            @PathVariable("userId") UUID userId
    );

    /**
     * Получение глобальной статистики
     */
    @GetMapping("/api/statistics/global")
    GlobalGameStatisticsDTO getGlobalGameStatistics();

    // Внутренние DTO классы для Game Engine Service

    @Setter
    @Getter
    class GameSessionDTO {
        private UUID id;
        private UUID questId;
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
        private List<ParticipantDTO> participants;
    }

    @Setter
    @Getter
    class ParticipantDTO {
        private UUID userId;
        private String username;
        private String joinTime;
        private String leaveTime;
        private Integer score;
        private Integer levelsCompleted;
        private Boolean isActive;
    }

    @Setter
    @Getter
    class QuestDTO {
        private UUID id;
        private String name;
        private String description;
        private String category;
        private String difficulty;
        private Integer maxScore;
        private Integer totalLevels;
        private String estimatedDuration;
    }

    @Setter
    @Getter
    class TeamGameStatisticsDTO {
        private Long totalSessions;
        private Long completedSessions;
        private Long activeSessions;
        private Double averageScore;
        private Double averageCompletionRate;
        private Integer totalScore;
        private String lastSessionDate;
        private String favoriteQuestCategory;
    }

    @Setter
    @Getter
    class UserGameStatisticsDTO {
        private Long totalSessions;
        private Long completedSessions;
        private Long activeSessions;
        private Double averageScore;
        private Double averageCompletionRate;
        private Integer totalScore;
        private String lastSessionDate;
        private String favoriteQuestCategory;
        private Integer totalPlayTime;
    }

    @Setter
    @Getter
    class TeamAchievementDTO {
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private String unlockedAt;
        private Integer points;
        private Long unlockedBy;
    }

    @Setter
    @Getter
    class UserAchievementDTO {
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private String unlockedAt;
        private Integer points;
    }

    @Setter
    @Getter
    class TeamLeaderboardDTO {
        private UUID teamId;
        private String teamName;
        private String teamTag;
        private Integer totalScore;
        private Integer completedSessions;
        private Double averageScore;
        private Integer rank;
    }

    @Setter
    @Getter
    class UserLeaderboardDTO {
        private UUID userId;
        private String username;
        private Integer totalScore;
        private Integer completedSessions;
        private Double averageScore;
        private Integer rank;
    }

    @Setter
    @Getter
    class GlobalGameStatisticsDTO {
        private Long totalSessions;
        private Long activeSessions;
        private Long completedSessions;
        private Double averageCompletionRate;
        private String mostPopularQuestCategory;
        private Long totalPlayers;
        private Long activePlayers;
    }

    // Request DTOs
    @Setter
    @Getter
    class CreateGameSessionRequest {
        private UUID questId;
        private UUID teamId;
        private Long startedBy;
    }

    @Setter
    @Getter
    class FinishGameSessionRequest {
        private String status;
        private Integer finalScore;
        private String finishReason;
    }

    @Setter
    @Getter
    class AddParticipantRequest {
        private UUID userId;
        private String joinMethod;
    }
}