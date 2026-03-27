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
    Team createTeam(String name, String description, Long captainId);
    Team updateTeamInfo(Long teamId, String name, String description);
    Team setTeamCaptain(Long teamId, Long newCaptainId);
    Team activateTeam(Long teamId);
    Team deactivateTeam(Long teamId);
    
    // Управление участниками
    TeamMember addMember(Long teamId, Long userId, TeamRole role);
    TeamMember removeMember(Long teamId, Long userId);
    TeamMember updateMemberRole(Long teamId, Long userId, TeamRole newRole);
    TeamMember activateMember(Long teamId, Long userId);
    TeamMember deactivateMember(Long teamId, Long userId);
    
    // Поиск и фильтрация команд
    Page<Team> getAllTeams(Pageable pageable);
    List<Team> getTeamsByCaptain(Long captainId);
    List<Team> getTeamsByNameContaining(String name);
    List<Team> getTeamsByRating(Double minRating, Double maxRating);
    List<Team> getTeamsByMemberCount(int minMembers, int maxMembers);
    
    // Поиск участников
    List<TeamMember> getTeamMembers(Long teamId);
    List<TeamMember> getActiveTeamMembers(Long teamId);
    List<TeamMember> getMembersByRole(Long teamId, TeamRole role);
    Optional<TeamMember> getTeamMember(Long teamId, Long userId);
    List<TeamMember> getUserTeams(Long userId);
    List<TeamMember> getUserActiveTeams(Long userId);
    
    // Статистика команд
    long getTotalTeamsCount();
    long getTeamsCountByCaptain(Long captainId);
    int getTeamMemberCount(Long teamId);
    int getActiveTeamMemberCount(Long teamId);
    double getAverageTeamRating();
    double getAverageTeamSize();
    
    // Рейтинги и статистика
    Team updateTeamRating(Long teamId, Double newRating);
    Double calculateTeamRating(Long teamId);
    List<Team> getTopTeamsByRating(int limit);
    List<Team> getTopTeamsByGamesPlayed(int limit);
    List<Team> getTopTeamsByWinRate(int limit);
    Integer getTeamRanking(Long teamId);
    
    // Игровая статистика
    Team updateGameStats(Long teamId, int gamesPlayed, int gamesWon, long playtimeSeconds);
    Double getTeamWinRate(Long teamId);
    Long getTeamTotalPlaytime(Long teamId);
    List<Team> getMostActiveTeams(int limit);
    List<Team> getMostExperiencedTeams(int limit);
    
    // Валидация и бизнес-логика
    boolean canCreateTeam(Long userId);
    boolean canJoinTeam(Long teamId, Long userId);
    boolean canLeaveTeam(Long teamId, Long userId);
    boolean canRemoveMember(Long teamId, Long captainId, Long memberUserId);
    boolean canChangeCaptain(Long teamId, Long currentCaptainId, Long newCaptainId);
    boolean isTeamFull(Long teamId);
    boolean isUserCaptain(Long teamId, Long userId);
    boolean isUserMember(Long teamId, Long userId);
    
    // Управление лимитами
    boolean isTeamMemberLimitReached(Long teamId);
    int getAvailableSlots(Long teamId);
    boolean canAddMoreMembers(Long teamId);
    Team updateMemberLimit(Long teamId, Integer newLimit);
    
    // Операции с приглашениями
    TeamMember inviteMember(Long teamId, Long userId, Long invitedBy);
    TeamMember acceptInvitation(Long teamId, Long userId);
    TeamMember rejectInvitation(Long teamId, Long userId);
    TeamMember cancelInvitation(Long teamId, Long userId);
    List<TeamMember> getPendingInvitations(Long teamId);
    List<TeamMember> getUserPendingInvitations(Long userId);
    
    // Командные операции
    List<Team> getTeamsByGameSession(Long sessionId);
    Team assignToGameSession(Long teamId, Long sessionId);
    Team removeFromGameSession(Long teamId, Long sessionId);
    List<Team> getAvailableTeamsForSession(Long sessionId);
    
    // Операции для администрирования
    List<Team> getAllTeamsForAdmin();
    void forceRemoveMember(Long teamId, Long userId, String reason);
    void suspendTeam(Long teamId, String reason);
    void unsuspendTeam(Long teamId);
    void deleteInactiveTeams(int inactiveDays);
    List<Team> getSuspiciousTeams(int limit);
    
    // Операции с кэшированием
    void cacheTeam(Team team);
    void evictTeamFromCache(Long teamId);
    Optional<Team> getCachedTeam(Long teamId);
    void cacheTeamMembers(Long teamId, List<TeamMember> members);
    void evictTeamMembersFromCache(Long teamId);
    
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
    List<Object[]> getTeamMemberStatistics(Long teamId);
    List<Object[]> getTeamPerformanceAnalysis(Long teamId);
    List<Object[]> getTeamGrowthMetrics();
    List<Object[]> getTeamActivityReport(Long teamId, Instant start, Instant end);
    
    // Операции для оптимизации
    void batchCreateTeams(List<Team> teams);
    void batchUpdateTeams(List<Team> teams);
    List<Team> getTeamsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateTeamId();
    void logTeamOperation(String operation, Long teamId, Long userId);
    boolean isValidTeamName(String name);
    String sanitizeTeamDescription(String description);
    
    // Операции с ролями
    List<TeamRole> getAvailableRoles();
    boolean isValidRoleTransition(TeamRole currentRole, TeamRole newRole);
    List<TeamMember> getMembersByRoleHierarchy(Long teamId);
    
    // Операции с историей
    List<String> getTeamHistory(Long teamId);
    void addTeamNote(Long teamId, String note, Long authorId);
    List<String> getTeamNotes(Long teamId);
    List<TeamMember> getFormerMembers(Long teamId);
    
    // Операции с поиском
    Page<Team> searchTeams(String keyword, Pageable pageable);
    List<Team> getRecommendedTeams(Long userId, int limit);
    List<Team> getSimilarTeams(Long teamId, int limit);
    List<Team> getTeamsBySkillLevel(String skillLevel);
    
    // Операции с настройками
    Team updateTeamSettings(Long teamId, Map<String, Object> settings);
    Map<String, Object> getTeamSettings(Long teamId);
    Team enableAutoApproval(Long teamId);
    Team disableAutoApproval(Long teamId);
    boolean isAutoApprovalEnabled(Long teamId);
    
    // Операции с соревнованиями
    List<Team> getTeamsInCompetition(Long competitionId);
    Team registerForCompetition(Long teamId, Long competitionId);
    Team unregisterFromCompetition(Long teamId, Long competitionId);
    List<Team> getTopTeamsInCompetition(Long competitionId, int limit);
    
    // Операции с достижениями
    List<String> getTeamAchievements(Long teamId);
    void addTeamAchievement(Long teamId, String achievementCode);
    void removeTeamAchievement(Long teamId, String achievementCode);
    boolean hasTeamAchievement(Long teamId, String achievementCode);
    
    // Операции с безопасностью
    boolean isTeamAccessAllowed(Long userId, Long teamId);
    boolean isTeamModificationAllowed(Long userId, Long teamId);
    void validateTeamData(Team team);
    void sanitizeTeamData(Team team);
    
    // Операции с мониторингом
    Map<String, Object> getTeamHealthMetrics(Long teamId);
    List<String> getTeamErrors(Long teamId);
    Map<String, Object> getTeamPerformanceStats(Long teamId);
    void monitorTeamActivity(Long teamId);
    
    // Операции с экспортом
    List<Team> exportTeams();
    List<TeamMember> exportTeamMembers(Long teamId);
    String generateTeamReport(Long teamId);
    String generateTeamsReport();
}