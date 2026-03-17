package dn.quest.teammanagement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
            @PathVariable("teamId") Long teamId,
            @RequestParam(value = "period", defaultValue = "30") int period
    );

    /**
     * Получение агрегированной статистики пользователя
     */
    @GetMapping("/api/statistics/users/{userId}/aggregated")
    UserAggregatedStatisticsDTO getUserAggregatedStatistics(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "period", defaultValue = "30") int period
    );

    /**
     * Получение глобальной статистики команд
     */
    @GetMapping("/api/statistics/teams/global")
    GlobalTeamStatisticsDTO getGlobalTeamStatistics();

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
            @PathVariable("teamId") Long teamId,
            @RequestParam("startDate") Instant startDate,
            @RequestParam("endDate") Instant endDate,
            @RequestParam(value = "granularity", defaultValue = "DAILY") String granularity
    );

    /**
     * Получение статистики пользователя по периодам
     */
    @GetMapping("/api/statistics/users/{userId}/history")
    List<UserStatisticsHistoryDTO> getUserStatisticsHistory(
            @PathVariable("userId") Long userId,
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
            @PathVariable("teamId") Long teamId,
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

    class TeamStatisticsDataDTO {
        private Long teamId;
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
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getTeamTag() { return teamTag; }
        public void setTeamTag(String teamTag) { this.teamTag = teamTag; }
        
        public Integer getMemberCount() { return memberCount; }
        public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
        
        public Integer getActiveMemberCount() { return activeMemberCount; }
        public void setActiveMemberCount(Integer activeMemberCount) { this.activeMemberCount = activeMemberCount; }
        
        public Long getTotalInvitationsSent() { return totalInvitationsSent; }
        public void setTotalInvitationsSent(Long totalInvitationsSent) { this.totalInvitationsSent = totalInvitationsSent; }
        
        public Long getTotalInvitationsAccepted() { return totalInvitationsAccepted; }
        public void setTotalInvitationsAccepted(Long totalInvitationsAccepted) { this.totalInvitationsAccepted = totalInvitationsAccepted; }
        
        public Long getTotalInvitationsDeclined() { return totalInvitationsDeclined; }
        public void setTotalInvitationsDeclined(Long totalInvitationsDeclined) { this.totalInvitationsDeclined = totalInvitationsDeclined; }
        
        public Double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
        
        public Integer getTotalGameSessions() { return totalGameSessions; }
        public void setTotalGameSessions(Integer totalGameSessions) { this.totalGameSessions = totalGameSessions; }
        
        public Integer getCompletedGameSessions() { return completedGameSessions; }
        public void setCompletedGameSessions(Integer completedGameSessions) { this.completedGameSessions = completedGameSessions; }
        
        public Double getAverageGameScore() { return averageGameScore; }
        public void setAverageGameScore(Double averageGameScore) { this.averageGameScore = averageGameScore; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public String getLastActivityAt() { return lastActivityAt; }
        public void setLastActivityAt(String lastActivityAt) { this.lastActivityAt = lastActivityAt; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    class UserStatisticsDataDTO {
        private Long userId;
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
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Integer getTeamCount() { return teamCount; }
        public void setTeamCount(Integer teamCount) { this.teamCount = teamCount; }
        
        public Integer getActiveTeamCount() { return activeTeamCount; }
        public void setActiveTeamCount(Integer activeTeamCount) { this.activeTeamCount = activeTeamCount; }
        
        public Integer getCaptainTeamCount() { return captainTeamCount; }
        public void setCaptainTeamCount(Integer captainTeamCount) { this.captainTeamCount = captainTeamCount; }
        
        public Integer getInvitationCount() { return invitationCount; }
        public void setInvitationCount(Integer invitationCount) { this.invitationCount = invitationCount; }
        
        public Integer getAcceptedInvitationCount() { return acceptedInvitationCount; }
        public void setAcceptedInvitationCount(Integer acceptedInvitationCount) { this.acceptedInvitationCount = acceptedInvitationCount; }
        
        public Integer getDeclinedInvitationCount() { return declinedInvitationCount; }
        public void setDeclinedInvitationCount(Integer declinedInvitationCount) { this.declinedInvitationCount = declinedInvitationCount; }
        
        public Double getTeamParticipationRate() { return teamParticipationRate; }
        public void setTeamParticipationRate(Double teamParticipationRate) { this.teamParticipationRate = teamParticipationRate; }
        
        public Integer getTotalGameSessions() { return totalGameSessions; }
        public void setTotalGameSessions(Integer totalGameSessions) { this.totalGameSessions = totalGameSessions; }
        
        public Integer getCompletedGameSessions() { return completedGameSessions; }
        public void setCompletedGameSessions(Integer completedGameSessions) { this.completedGameSessions = completedGameSessions; }
        
        public Double getAverageGameScore() { return averageGameScore; }
        public void setAverageGameScore(Double averageGameScore) { this.averageGameScore = averageGameScore; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public String getLastActivityAt() { return lastActivityAt; }
        public void setLastActivityAt(String lastActivityAt) { this.lastActivityAt = lastActivityAt; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    class InvitationStatisticsDataDTO {
        private Long invitationId;
        private Long teamId;
        private String teamName;
        private Long userId;
        private String username;
        private Long invitedBy;
        private String invitedByUsername;
        private String status;
        private String createdAt;
        private String respondedAt;
        private String responseMessage;
        
        // Getters and setters
        public Long getInvitationId() { return invitationId; }
        public void setInvitationId(Long invitationId) { this.invitationId = invitationId; }
        
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Long getInvitedBy() { return invitedBy; }
        public void setInvitedBy(Long invitedBy) { this.invitedBy = invitedBy; }
        
        public String getInvitedByUsername() { return invitedByUsername; }
        public void setInvitedByUsername(String invitedByUsername) { this.invitedByUsername = invitedByUsername; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getRespondedAt() { return respondedAt; }
        public void setRespondedAt(String respondedAt) { this.respondedAt = respondedAt; }
        
        public String getResponseMessage() { return responseMessage; }
        public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    }

    class GameSessionStatisticsDataDTO {
        private Long sessionId;
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
        private Integer participantCount;
        private List<Long> participantIds;
        
        // Getters and setters
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        
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
        
        public Integer getParticipantCount() { return participantCount; }
        public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }
        
        public List<Long> getParticipantIds() { return participantIds; }
        public void setParticipantIds(List<Long> participantIds) { this.participantIds = participantIds; }
    }

    class TeamAggregatedStatisticsDTO {
        private Long teamId;
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
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public Integer getTotalMembers() { return totalMembers; }
        public void setTotalMembers(Integer totalMembers) { this.totalMembers = totalMembers; }
        
        public Integer getActiveMembers() { return activeMembers; }
        public void setActiveMembers(Integer activeMembers) { this.activeMembers = activeMembers; }
        
        public Long getTotalInvitations() { return totalInvitations; }
        public void setTotalInvitations(Long totalInvitations) { this.totalInvitations = totalInvitations; }
        
        public Double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
        
        public Integer getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public String getMostActiveDay() { return mostActiveDay; }
        public void setMostActiveDay(String mostActiveDay) { this.mostActiveDay = mostActiveDay; }
        
        public String getPeakActivityHour() { return peakActivityHour; }
        public void setPeakActivityHour(String peakActivityHour) { this.peakActivityHour = peakActivityHour; }
    }

    class UserAggregatedStatisticsDTO {
        private Long userId;
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
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public Integer getTotalTeams() { return totalTeams; }
        public void setTotalTeams(Integer totalTeams) { this.totalTeams = totalTeams; }
        
        public Integer getActiveTeams() { return activeTeams; }
        public void setActiveTeams(Integer activeTeams) { this.activeTeams = activeTeams; }
        
        public Long getTotalInvitations() { return totalInvitations; }
        public void setTotalInvitations(Long totalInvitations) { this.totalInvitations = totalInvitations; }
        
        public Double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
        
        public Integer getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public Integer getTotalScore() { return totalScore; }
        public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
        
        public String getMostActiveDay() { return mostActiveDay; }
        public void setMostActiveDay(String mostActiveDay) { this.mostActiveDay = mostActiveDay; }
        
        public String getPeakActivityHour() { return peakActivityHour; }
        public void setPeakActivityHour(String peakActivityHour) { this.peakActivityHour = peakActivityHour; }
    }

    class GlobalTeamStatisticsDTO {
        private Long totalTeams;
        private Long activeTeams;
        private Double averageTeamSize;
        private Long totalInvitations;
        private Double averageAcceptanceRate;
        private Long totalSessions;
        private Double averageScore;
        private String mostActiveTeamCategory;
        
        // Getters and setters
        public Long getTotalTeams() { return totalTeams; }
        public void setTotalTeams(Long totalTeams) { this.totalTeams = totalTeams; }
        
        public Long getActiveTeams() { return activeTeams; }
        public void setActiveTeams(Long activeTeams) { this.activeTeams = activeTeams; }
        
        public Double getAverageTeamSize() { return averageTeamSize; }
        public void setAverageTeamSize(Double averageTeamSize) { this.averageTeamSize = averageTeamSize; }
        
        public Long getTotalInvitations() { return totalInvitations; }
        public void setTotalInvitations(Long totalInvitations) { this.totalInvitations = totalInvitations; }
        
        public Double getAverageAcceptanceRate() { return averageAcceptanceRate; }
        public void setAverageAcceptanceRate(Double averageAcceptanceRate) { this.averageAcceptanceRate = averageAcceptanceRate; }
        
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public String getMostActiveTeamCategory() { return mostActiveTeamCategory; }
        public void setMostActiveTeamCategory(String mostActiveTeamCategory) { this.mostActiveTeamCategory = mostActiveTeamCategory; }
    }

    class GlobalUserStatisticsDTO {
        private Long totalUsers;
        private Long activeUsers;
        private Double averageTeamsPerUser;
        private Long totalInvitations;
        private Double averageAcceptanceRate;
        private Long totalSessions;
        private Double averageScore;
        private String mostActiveUser;
        
        // Getters and setters
        public Long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
        
        public Long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
        
        public Double getAverageTeamsPerUser() { return averageTeamsPerUser; }
        public void setAverageTeamsPerUser(Double averageTeamsPerUser) { this.averageTeamsPerUser = averageTeamsPerUser; }
        
        public Long getTotalInvitations() { return totalInvitations; }
        public void setTotalInvitations(Long totalInvitations) { this.totalInvitations = totalInvitations; }
        
        public Double getAverageAcceptanceRate() { return averageAcceptanceRate; }
        public void setAverageAcceptanceRate(Double averageAcceptanceRate) { this.averageAcceptanceRate = averageAcceptanceRate; }
        
        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        
        public String getMostActiveUser() { return mostActiveUser; }
        public void setMostActiveUser(String mostActiveUser) { this.mostActiveUser = mostActiveUser; }
    }

    class StatisticsTrendsDTO {
        private String metric;
        private String period;
        private List<TrendDataPoint> dataPoints;
        private Double growthRate;
        private String trend;
        
        // Getters and setters
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public List<TrendDataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<TrendDataPoint> dataPoints) { this.dataPoints = dataPoints; }
        
        public Double getGrowthRate() { return growthRate; }
        public void setGrowthRate(Double growthRate) { this.growthRate = growthRate; }
        
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    class TrendDataPoint {
        private String timestamp;
        private Double value;
        
        // Getters and setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }

    class TeamRankingDTO {
        private Long teamId;
        private String teamName;
        private String teamTag;
        private Integer rank;
        private Double score;
        private String metric;
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getTeamTag() { return teamTag; }
        public void setTeamTag(String teamTag) { this.teamTag = teamTag; }
        
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
    }

    class UserRankingDTO {
        private Long userId;
        private String username;
        private Integer rank;
        private Double score;
        private String metric;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
    }

    class TeamStatisticsHistoryDTO {
        private String timestamp;
        private Integer memberCount;
        private Integer activeMemberCount;
        private Long totalInvitations;
        private Double acceptanceRate;
        private Integer totalSessions;
        private Double averageScore;
        
        // Getters and setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public Integer getMemberCount() { return memberCount; }
        public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
        
        public Integer getActiveMemberCount() { return activeMemberCount; }
        public void setActiveMemberCount(Integer activeMemberCount) { this.activeMemberCount = activeMemberCount; }
        
        public Long getTotalInvitations() { return totalInvitations; }
        public void setTotalInvitations(Long totalInvitations) { this.totalInvitations = totalInvitations; }
        
        public Double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
        
        public Integer getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    }

    class UserStatisticsHistoryDTO {
        private String timestamp;
        private Integer teamCount;
        private Integer activeTeamCount;
        private Long totalInvitations;
        private Double acceptanceRate;
        private Integer totalSessions;
        private Double averageScore;
        
        // Getters and setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public Integer getTeamCount() { return teamCount; }
        public void setTeamCount(Integer teamCount) { this.teamCount = teamCount; }
        
        public Integer getActiveTeamCount() { return activeTeamCount; }
        public void setActiveTeamCount(Integer activeTeamCount) { this.activeTeamCount = activeTeamCount; }
        
        public Long getTotalInvitations() { return totalInvitations; }
        public void setTotalInvitations(Long totalInvitations) { this.totalInvitations = totalInvitations; }
        
        public Double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
        
        public Integer getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
        
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    }

    class TeamComparisonDTO {
        private List<TeamComparisonItemDTO> teams;
        private Map<String, Object> comparisonMetrics;
        
        // Getters and setters
        public List<TeamComparisonItemDTO> getTeams() { return teams; }
        public void setTeams(List<TeamComparisonItemDTO> teams) { this.teams = teams; }
        
        public Map<String, Object> getComparisonMetrics() { return comparisonMetrics; }
        public void setComparisonMetrics(Map<String, Object> comparisonMetrics) { this.comparisonMetrics = comparisonMetrics; }
    }

    class TeamComparisonItemDTO {
        private Long teamId;
        private String teamName;
        private Map<String, Double> metrics;
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public Map<String, Double> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Double> metrics) { this.metrics = metrics; }
    }

    class UserComparisonDTO {
        private List<UserComparisonItemDTO> users;
        private Map<String, Object> comparisonMetrics;
        
        // Getters and setters
        public List<UserComparisonItemDTO> getUsers() { return users; }
        public void setUsers(List<UserComparisonItemDTO> users) { this.users = users; }
        
        public Map<String, Object> getComparisonMetrics() { return comparisonMetrics; }
        public void setComparisonMetrics(Map<String, Object> comparisonMetrics) { this.comparisonMetrics = comparisonMetrics; }
    }

    class UserComparisonItemDTO {
        private Long userId;
        private String username;
        private Map<String, Double> metrics;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public Map<String, Double> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Double> metrics) { this.metrics = metrics; }
    }

    class TeamForecastDTO {
        private Long teamId;
        private String teamName;
        private String period;
        private Map<String, List<ForecastDataPoint>> forecasts;
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public Map<String, List<ForecastDataPoint>> getForecasts() { return forecasts; }
        public void setForecasts(Map<String, List<ForecastDataPoint>> forecasts) { this.forecasts = forecasts; }
    }

    class ForecastDataPoint {
        private String timestamp;
        private Double predictedValue;
        private Double confidenceInterval;
        
        // Getters and setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public Double getPredictedValue() { return predictedValue; }
        public void setPredictedValue(Double predictedValue) { this.predictedValue = predictedValue; }
        
        public Double getConfidenceInterval() { return confidenceInterval; }
        public void setConfidenceInterval(Double confidenceInterval) { this.confidenceInterval = confidenceInterval; }
    }

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
        
        // Getters and setters
        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        
        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public Double getExpectedValue() { return expectedValue; }
        public void setExpectedValue(Double expectedValue) { this.expectedValue = expectedValue; }
        
        public Double getActualValue() { return actualValue; }
        public void setActualValue(Double actualValue) { this.actualValue = actualValue; }
        
        public Double getDeviationPercentage() { return deviationPercentage; }
        public void setDeviationPercentage(Double deviationPercentage) { this.deviationPercentage = deviationPercentage; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
