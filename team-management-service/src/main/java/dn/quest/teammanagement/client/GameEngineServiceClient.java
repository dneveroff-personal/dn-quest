package dn.quest.teammanagement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign клиент для взаимодействия с Game Engine Service
 */
@FeignClient(name = "game-engine-service", url = "${app.services.game-engine-service.url}")
public interface GameEngineServiceClient {

    /**
     * Получение информации о игровой сессии по ID
     */
    @GetMapping("/api/game-sessions/{id}")
    GameSessionDTO getGameSessionById(@PathVariable("id") Long sessionId);

    /**
     * Получение списка игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}")
    List<GameSessionDTO> getGameSessionsByTeam(
            @PathVariable("teamId") Long teamId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение списка игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}")
    List<GameSessionDTO> getGameSessionsByUser(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение активных игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/active")
    List<GameSessionDTO> getActiveGameSessionsByTeam(@PathVariable("teamId") Long teamId);

    /**
     * Получение активных игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/active")
    List<GameSessionDTO> getActiveGameSessionsByUser(@PathVariable("userId") Long userId);

    /**
     * Получение завершенных игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/completed")
    List<GameSessionDTO> getCompletedGameSessionsByTeam(
            @PathVariable("teamId") Long teamId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение статистики игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/statistics")
    TeamGameStatisticsDTO getTeamGameStatistics(@PathVariable("teamId") Long teamId);

    /**
     * Получение статистики игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/statistics")
    UserGameStatisticsDTO getUserGameStatistics(@PathVariable("userId") Long userId);

    /**
     * Получение количества игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/count")
    long getTeamGameSessionsCount(@PathVariable("teamId") Long teamId);

    /**
     * Получение количества активных игровых сессий для команды
     */
    @GetMapping("/api/game-sessions/team/{teamId}/active/count")
    long getTeamActiveGameSessionsCount(@PathVariable("teamId") Long teamId);

    /**
     * Получение количества игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/count")
    long getUserGameSessionsCount(@PathVariable("userId") Long userId);

    /**
     * Получение количества активных игровых сессий для пользователя
     */
    @GetMapping("/api/game-sessions/user/{userId}/active/count")
    long getUserActiveGameSessionsCount(@PathVariable("userId") Long userId);

    /**
     * Проверка, участвует ли команда в активной игровой сессии
     */
    @GetMapping("/api/game-sessions/team/{teamId}/has-active")
    boolean teamHasActiveGameSession(@PathVariable("teamId") Long teamId);

    /**
     * Проверка, участвует ли пользователь в активной игровой сессии
     */
    @GetMapping("/api/game-sessions/user/{userId}/has-active")
    boolean userHasActiveGameSession(@PathVariable("userId") Long userId);

    /**
     * Получение информации о квесте по ID
     */
    @GetMapping("/api/quests/{id}")
    QuestDTO getQuestById(@PathVariable("id") Long questId);

    /**
     * Получение списка доступных квестов для команды
     */
    @GetMapping("/api/quests/available/team/{teamId}")
    List<QuestDTO> getAvailableQuestsForTeam(@PathVariable("teamId") Long teamId);

    /**
     * Получение списка завершенных квестов для команды
     */
    @GetMapping("/api/quests/completed/team/{teamId}")
    List<QuestDTO> getCompletedQuestsForTeam(@PathVariable("teamId") Long teamId);

    /**
     * Получение достижений команды
     */
    @GetMapping("/api/achievements/team/{teamId}")
    List<TeamAchievementDTO> getTeamAchievements(@PathVariable("teamId") Long teamId);

    /**
     * Получение достижений пользователя
     */
    @GetMapping("/api/achievements/user/{userId}")
    List<UserAchievementDTO> getUserAchievements(@PathVariable("userId") Long userId);

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
            @PathVariable("id") Long sessionId,
            @RequestBody FinishGameSessionRequest request
    );

    /**
     * Присоединение участника к игровой сессии
     */
    @PostMapping("/api/game-sessions/{id}/participants")
    GameSessionDTO addParticipantToGameSession(
            @PathVariable("id") Long sessionId,
            @RequestBody AddParticipantRequest request
    );

    /**
     * Удаление участника из игровой сессии
     */
    @DeleteMapping("/api/game-sessions/{id}/participants/{userId}")
    GameSessionDTO removeParticipantFromGameSession(
            @PathVariable("id") Long sessionId,
            @PathVariable("userId") Long userId
    );

    /**
     * Получение глобальной статистики
     */
    @GetMapping("/api/statistics/global")
    GlobalGameStatisticsDTO getGlobalGameStatistics();

    // Внутренние DTO классы для Game Engine Service

    class GameSessionDTO {
        private Long id;
        private Long questId;
        private String questName;
        private Long teamId;
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
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }
        
        public String getQuestName() { return questName; }
        public void setQuestName(String questName) { this.questName = questName; }
        
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        
        public Integer getMaxScore() { return maxScore; }
        public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
        
        public Double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }
        
        public Integer getLevelsCompleted() { return levelsCompleted; }
        public void setLevelsCompleted(Integer levelsCompleted) { this.levelsCompleted = levelsCompleted; }
        
        public Integer getTotalLevels() { return totalLevels; }
        public void setTotalLevels(Integer totalLevels) { this.totalLevels = totalLevels; }
        
        public List<ParticipantDTO> getParticipants() { return participants; }
        public void setParticipants(List<ParticipantDTO> participants) { this.participants = participants; }
    }

    class ParticipantDTO {
        private Long userId;
        private String username;
        private String joinTime;
        private String leaveTime;
        private Integer score;
        private Integer levelsCompleted;
        private Boolean isActive;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getJoinTime() { return joinTime; }
        public void setJoinTime(String joinTime) { this.joinTime = joinTime; }
        
        public String getLeaveTime() { return leaveTime; }
        public void setLeaveTime(String leaveTime) { this.leaveTime = leaveTime; }
        
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        
        public Integer getLevelsCompleted() { return levelsCompleted; }
        public void setLevelsCompleted(Integer levelsCompleted) { this.levelsCompleted = levelsCompleted; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    class QuestDTO {
        private Long id;
        private String name;
        private String description;
        private String category;
        private String difficulty;
        private Integer maxScore;
        private Integer totalLevels;
        private String estimatedDuration;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        
        public Integer getMaxScore() { return maxScore; }
        public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
        
        public Integer getTotalLevels() { return totalLevels; }
        public void setTotalLevels(Integer totalLevels) { this.totalLevels = totalLevels; }
        
        public String getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(String estimatedDuration) { this.estimatedDuration = estimatedDuration; }
    }

    class TeamGameStatisticsDTO {
        private Long totalSessions;
        private Long completedSessions;
        private Long activeSessions;
        private Double averageScore;
        private Double averageCompletionRate;
        private Integer totalScore;
        private String lastSessionDate;
        private String favoriteQuestCategory;
        
        // Getters and setters
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }
        
        public Long getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(Long completedSessions) { this.completedSessions = completedSessions; }
        
        public Long getActiveSessions() { return activeSessions; }
        public void setActiveSessions(Long activeSessions) { this.activeSessions = activeSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Double getAverageCompletionRate() { return averageCompletionRate; }
        public void setAverageCompletionRate(Double averageCompletionRate) { this.averageCompletionRate = averageCompletionRate; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public String getLastSessionDate() { return lastSessionDate; }
        public void setLastSessionDate(String lastSessionDate) { this.lastSessionDate = lastSessionDate; }
        
        public String getFavoriteQuestCategory() { return favoriteQuestCategory; }
        public void setFavoriteQuestCategory(String favoriteQuestCategory) { this.favoriteQuestCategory = favoriteQuestCategory; }
    }

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
        
        // Getters and setters
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }
        
        public Long getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(Long completedSessions) { this.completedSessions = completedSessions; }
        
        public Long getActiveSessions() { return activeSessions; }
        public void setActiveSessions(Long activeSessions) { this.activeSessions = activeSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Double getAverageCompletionRate() { return averageCompletionRate; }
        public void setAverageCompletionRate(Double averageCompletionRate) { this.averageCompletionRate = averageCompletionRate; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public String getLastSessionDate() { return lastSessionDate; }
        public void setLastSessionDate(String lastSessionDate) { this.lastSessionDate = lastSessionDate; }
        
        public String getFavoriteQuestCategory() { return favoriteQuestCategory; }
        public void setFavoriteQuestCategory(String favoriteQuestCategory) { this.favoriteQuestCategory = favoriteQuestCategory; }
        
        public Integer getTotalPlayTime() { return totalPlayTime; }
        public void setTotalPlayTime(Integer totalPlayTime) { this.totalPlayTime = totalPlayTime; }
    }

    class TeamAchievementDTO {
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private String unlockedAt;
        private Integer points;
        private Long unlockedBy;
        
        // Getters and setters
        public Long getAchievementId() { return achievementId; }
        public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
        
        public String getAchievementName() { return achievementName; }
        public void setAchievementName(String achievementName) { this.achievementName = achievementName; }
        
        public String getAchievementDescription() { return achievementDescription; }
        public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }
        
        public String getAchievementType() { return achievementType; }
        public void setAchievementType(String achievementType) { this.achievementType = achievementType; }
        
        public String getUnlockedAt() { return unlockedAt; }
        public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
        
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
        
        public Long getUnlockedBy() { return unlockedBy; }
        public void setUnlockedBy(Long unlockedBy) { this.unlockedBy = unlockedBy; }
    }

    class UserAchievementDTO {
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private String unlockedAt;
        private Integer points;
        
        // Getters and setters
        public Long getAchievementId() { return achievementId; }
        public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
        
        public String getAchievementName() { return achievementName; }
        public void setAchievementName(String achievementName) { this.achievementName = achievementName; }
        
        public String getAchievementDescription() { return achievementDescription; }
        public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }
        
        public String getAchievementType() { return achievementType; }
        public void setAchievementType(String achievementType) { this.achievementType = achievementType; }
        
        public String getUnlockedAt() { return unlockedAt; }
        public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
        
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
    }

    class TeamLeaderboardDTO {
        private Long teamId;
        private String teamName;
        private String teamTag;
        private Integer totalScore;
        private Integer completedSessions;
        private Double averageScore;
        private Integer rank;
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getTeamTag() { return teamTag; }
        public void setTeamTag(String teamTag) { this.teamTag = teamTag; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public Integer getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(Integer completedSessions) { this.completedSessions = completedSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
    }

    class UserLeaderboardDTO {
        private Long userId;
        private String username;
        private Integer totalScore;
        private Integer completedSessions;
        private Double averageScore;
        private Integer rank;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public Integer getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(Integer completedSessions) { this.completedSessions = completedSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
    }

    class GlobalGameStatisticsDTO {
        private Long totalSessions;
        private Long activeSessions;
        private Long completedSessions;
        private Double averageCompletionRate;
        private String mostPopularQuestCategory;
        private Long totalPlayers;
        private Long activePlayers;
        
        // Getters and setters
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }
        
        public Long getActiveSessions() { return activeSessions; }
        public void setActiveSessions(Long activeSessions) { this.activeSessions = activeSessions; }
        
        public Long getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(Long completedSessions) { this.completedSessions = completedSessions; }
        
        public Double getAverageCompletionRate() { return averageCompletionRate; }
        public void setAverageCompletionRate(Double averageCompletionRate) { this.averageCompletionRate = averageCompletionRate; }
        
        public String getMostPopularQuestCategory() { return mostPopularQuestCategory; }
        public void setMostPopularQuestCategory(String mostPopularQuestCategory) { this.mostPopularQuestCategory = mostPopularQuestCategory; }
        
        public Long getTotalPlayers() { return totalPlayers; }
        public void setTotalPlayers(Long totalPlayers) { this.totalPlayers = totalPlayers; }
        
        public Long getActivePlayers() { return activePlayers; }
        public void setActivePlayers(Long activePlayers) { this.activePlayers = activePlayers; }
    }

    // Request DTOs
    class CreateGameSessionRequest {
        private Long questId;
        private Long teamId;
        private Long startedBy;
        
        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }
        
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public Long getStartedBy() { return startedBy; }
        public void setStartedBy(Long startedBy) { this.startedBy = startedBy; }
    }

    class FinishGameSessionRequest {
        private String status;
        private Integer finalScore;
        private String finishReason;
        
        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Integer getFinalScore() { return finalScore; }
        public void setFinalScore(Integer finalScore) { this.finalScore = finalScore; }
        
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }

    class AddParticipantRequest {
        private Long userId;
        private String joinMethod;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getJoinMethod() { return joinMethod; }
        public void setJoinMethod(String joinMethod) { this.joinMethod = joinMethod; }
    }
}