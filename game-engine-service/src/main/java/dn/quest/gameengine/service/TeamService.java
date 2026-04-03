package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.Team;
import dn.quest.gameengine.entity.TeamMember;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления командами
 */
public interface TeamService {

    // Базовые операции CRUD
    Team createTeam(Team team);
    Optional<Team> getTeamById(Long id);
    Team updateTeam(Team team);
    void deleteTeam(Long id);
    
    // Управление командами
    Team createTeam(String name, String description, UUID captainId);
    Team updateTeamInfo(UUID teamId, String name, String description);
    Team setTeamCaptain(UUID teamId, Long newCaptainId);
    Team activateTeam(UUID teamId);
    Team deactivateTeam(UUID teamId);
    
    // Управление участниками
    TeamMember addMember(UUID teamId, UUID userId, TeamRole role);
    TeamMember removeMember(UUID teamId, UUID userId);
    TeamMember updateMemberRole(UUID teamId, UUID userId, TeamRole newRole);
    TeamMember activateMember(UUID teamId, UUID userId);
    TeamMember deactivateMember(UUID teamId, UUID userId);
    
    // Поиск и фильтрация команд
    Page<Team> getAllTeams(Pageable pageable);
    List<Team> getTeamsByCaptain(UUID captainId);
    List<Team> getTeamsByNameContaining(String name);
    List<Team> getTeamsByRating(Double minRating, Double maxRating);
    List<Team> getTeamsByMemberCount(int minMembers, int maxMembers);
    
    // Поиск участников
    List<TeamMember> getTeamMembers(UUID teamId);
    List<TeamMember> getActiveTeamMembers(UUID teamId);
    List<TeamMember> getMembersByRole(UUID teamId, TeamRole role);
    Optional<TeamMember> getTeamMember(UUID teamId, UUID userId);
    List<TeamMember> getUserTeams(UUID userId);
    List<TeamMember> getUserActiveTeams(UUID userId);
    
    // Статистика команд
    long getTotalTeamsCount();
    long getTeamsCountByCaptain(UUID captainId);
    int getTeamMemberCount(UUID teamId);
    int getActiveTeamMemberCount(UUID teamId);
    double getAverageTeamRating();
    double getAverageTeamSize();
    
    // Рейтинги и статистика
    Team updateTeamRating(UUID teamId, Double newRating);
    Double calculateTeamRating(UUID teamId);
    List<Team> getTopTeamsByRating(int limit);
    List<Team> getTopTeamsByGamesPlayed(int limit);
    List<Team> getTopTeamsByWinRate(int limit);
    Integer getTeamRanking(UUID teamId);
    
    // Игровая статистика
    Team updateGameStats(UUID teamId, int gamesPlayed, int gamesWon, long playtimeSeconds);
    Double getTeamWinRate(UUID teamId);
    Long getTeamTotalPlaytime(UUID teamId);
    List<Team> getMostActiveTeams(int limit);
    List<Team> getMostExperiencedTeams(int limit);
    
    // Валидация и бизнес-логика
    boolean canCreateTeam(UUID userId);
    boolean canJoinTeam(UUID teamId, UUID userId);
    boolean canLeaveTeam(UUID teamId, UUID userId);
    boolean canRemoveMember(UUID teamId, UUID captainId, Long memberUserId);
    boolean canChangeCaptain(UUID teamId, Long currentCaptainId, Long newCaptainId);
    boolean isTeamFull(UUID teamId);
    boolean isUserCaptain(UUID teamId, UUID userId);
    boolean isUserMember(UUID teamId, UUID userId);
    
    // Управление лимитами
    boolean isTeamMemberLimitReached(UUID teamId);
    int getAvailableSlots(UUID teamId);
    boolean canAddMoreMembers(UUID teamId);
    Team updateMemberLimit(UUID teamId, Integer newLimit);
    
    // Операции с приглашениями
    TeamMember inviteMember(UUID teamId, UUID userId, Long invitedBy);
    TeamMember acceptInvitation(UUID teamId, UUID userId);
    TeamMember rejectInvitation(UUID teamId, UUID userId);
    TeamMember cancelInvitation(UUID teamId, UUID userId);
    List<TeamMember> getPendingInvitations(UUID teamId);
    List<TeamMember> getUserPendingInvitations(UUID userId);
    
    // Командные операции
    List<Team> getTeamsByGameSession(Long sessionId);
    Team assignToGameSession(UUID teamId, Long sessionId);
    Team removeFromGameSession(UUID teamId, Long sessionId);
    List<Team> getAvailableTeamsForSession(Long sessionId);
    
    // Операции для администрирования
    List<Team> getAllTeamsForAdmin();
    void forceRemoveMember(UUID teamId, UUID userId, String reason);
    void suspendTeam(UUID teamId, String reason);
    void unsuspendTeam(UUID teamId);
    void deleteInactiveTeams(int inactiveDays);
    List<Team> getSuspiciousTeams(int limit);
    
    // Операции с кэшированием
    void cacheTeam(Team team);
    void evictTeamFromCache(UUID teamId);
    Optional<Team> getCachedTeam(UUID teamId);
    void cacheTeamMembers(UUID teamId, List<TeamMember> members);
    void evictTeamMembersFromCache(UUID teamId);
    
    // Операции с событиями
    void publishTeamCreatedEvent(Team team);
    void publishTeamUpdatedEvent(Team team);
    void publishTeamDeletedEvent(Team team);
    void publishMemberAddedEvent(TeamMember member);
    void publishMemberRemovedEvent(TeamMember member);
    void publishMemberRoleChangedEvent(TeamMember member);
    void publishCaptainChangedEvent(Team team, Long oldCaptainId, Long newCaptainId);
    
    // Интеграция с другими сервисами
    void notifyTeamMembers(Team team, String message);
    void notifyCaptain(Team team, String message);
    void updateTeamStatistics(Team team);
    void syncTeamWithExternalServices(Team team);
    
    // Аналитика и отчеты
    List<Object[]> getTeamStatisticsByDay(Instant start, Instant end);
    List<Object[]> getTeamMemberStatistics(UUID teamId);
    List<Object[]> getTeamPerformanceAnalysis(UUID teamId);
    List<Object[]> getTeamGrowthMetrics();
    List<Object[]> getTeamActivityReport(UUID teamId, Instant start, Instant end);
    
    // Операции для оптимизации
    void batchCreateTeams(List<Team> teams);
    void batchUpdateTeams(List<Team> teams);
    List<Team> getTeamsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateTeamId();
    void logTeamOperation(String operation, UUID teamId, UUID userId);
    boolean isValidTeamName(String name);
    String sanitizeTeamDescription(String description);
    
    // Операции с ролями
    List<TeamRole> getAvailableRoles();
    boolean isValidRoleTransition(TeamRole currentRole, TeamRole newRole);
    List<TeamMember> getMembersByRoleHierarchy(UUID teamId);
    
    // Операции с историей
    List<String> getTeamHistory(UUID teamId);
    void addTeamNote(UUID teamId, String note, UUID authorId);
    List<String> getTeamNotes(UUID teamId);
    List<TeamMember> getFormerMembers(UUID teamId);
    
    // Операции с поиском
    Page<Team> searchTeams(String keyword, Pageable pageable);
    List<Team> getRecommendedTeams(UUID userId, int limit);
    List<Team> getSimilarTeams(UUID teamId, int limit);
    List<Team> getTeamsBySkillLevel(String skillLevel);
    
    // Операции с настройками
    Team updateTeamSettings(UUID teamId, Map<String, Object> settings);
    Map<String, Object> getTeamSettings(UUID teamId);
    Team enableAutoApproval(UUID teamId);
    Team disableAutoApproval(UUID teamId);
    boolean isAutoApprovalEnabled(UUID teamId);
    
    // Операции с соревнованиями
    List<Team> getTeamsInCompetition(Long competitionId);
    Team registerForCompetition(UUID teamId, Long competitionId);
    Team unregisterFromCompetition(UUID teamId, Long competitionId);
    List<Team> getTopTeamsInCompetition(Long competitionId, int limit);
    
    // Операции с достижениями
    List<String> getTeamAchievements(UUID teamId);
    void addTeamAchievement(UUID teamId, String achievementCode);
    void removeTeamAchievement(UUID teamId, String achievementCode);
    boolean hasTeamAchievement(UUID teamId, String achievementCode);
    
    // Операции с безопасностью
    boolean isTeamAccessAllowed(UUID userId, UUID teamId);
    boolean isTeamModificationAllowed(UUID userId, UUID teamId);
    void validateTeamData(Team team);
    void sanitizeTeamData(Team team);
    
    // Операции с мониторингом
    Map<String, Object> getTeamHealthMetrics(UUID teamId);
    List<String> getTeamErrors(UUID teamId);
    Map<String, Object> getTeamPerformanceStats(UUID teamId);
    void monitorTeamActivity(UUID teamId);
    
    // Операции с экспортом
    List<Team> exportTeams();
    List<TeamMember> exportTeamMembers(UUID teamId);
    String generateTeamReport(UUID teamId);
    String generateTeamsReport();
}