package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления пользователями
 */
public interface UserService {

    // Базовые операции CRUD
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    User updateUser(User user);
    void deleteUser(Long id);
    
    // Управление профилем
    User updateProfile(Long userId, String firstName, String lastName, String bio);
    User updateAvatar(Long userId, String avatarUrl);
    User updateSettings(Long userId, Map<String, Object> settings);
    User changePassword(Long userId, String oldPassword, String newPassword);
    User deactivateUser(Long userId);
    User activateUser(Long userId);
    
    // Поиск и фильтрация пользователей
    Page<User> getAllUsers(Pageable pageable);
    List<User> getActiveUsers();
    List<User> getUsersByRole(UserRole role);
    List<User> getUsersByNameContaining(String name);
    List<User> getUsersByEmailContaining(String email);
    List<User> getUsersByRating(Double minRating, Double maxRating);
    List<User> getUsersByRegistrationDate(Instant start, Instant end);
    List<User> getUsersByLastActivity(Instant since);
    
    // Статистика пользователей
    long getTotalUsersCount();
    long getActiveUsersCount();
    long getUsersCountByRole(UserRole role);
    long getUsersRegisteredToday();
    long getUsersRegisteredThisWeek();
    long getUsersRegisteredThisMonth();
    double getAverageUserRating();
    List<User> getMostActiveUsers(int limit);
    List<User> getHighestRatedUsers(int limit);
    
    // Игровая статистика
    User updateGameStats(Long userId, int gamesPlayed, int gamesWon, long playtimeSeconds);
    Double getUserWinRate(Long userId);
    Long getUserTotalPlaytime(Long userId);
    int getUserGamesPlayed(Long userId);
    int getUserGamesWon(Long userId);
    List<User> getTopPlayersByScore(int limit);
    List<User> getTopPlayersByTime(int limit);
    List<User> getTopPlayersByWinRate(int limit);
    
    // Рейтинги и достижения
    User updateUserRating(Long userId, Double newRating);
    Double calculateUserRating(Long userId);
    Integer getUserRanking(Long userId);
    List<String> getUserAchievements(Long userId);
    void addAchievement(Long userId, String achievementCode);
    void removeAchievement(Long userId, String achievementCode);
    boolean hasAchievement(Long userId, String achievementCode);
    
    // Валидация и бизнес-логика
    boolean canCreateUser(String username, String email);
    boolean canUpdateUser(Long userId, Long editorId);
    boolean canDeleteUser(Long userId, Long deleterId);
    boolean isValidUsername(String username);
    boolean isValidEmail(String email);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    
    // Управление ролями
    User assignRole(Long userId, UserRole role);
    User removeRole(Long userId, UserRole role);
    List<UserRole> getUserRoles(Long userId);
    boolean hasRole(Long userId, UserRole role);
    boolean hasAnyRole(Long userId, List<UserRole> roles);
    
    // Управление сессиями
    List<Long> getUserActiveSessions(Long userId);
    List<Long> getUserCompletedSessions(Long userId);
    void addUserToSession(Long userId, Long sessionId);
    void removeUserFromSession(Long userId, Long sessionId);
    boolean isUserInSession(Long userId, Long sessionId);
    
    // Управление командами
    List<Long> getUserTeams(Long userId);
    List<Long> getUserOwnedTeams(Long userId);
    void addUserToTeam(Long userId, Long teamId);
    void removeUserFromTeam(Long userId, Long teamId);
    boolean isUserInTeam(Long userId, Long teamId);
    boolean isUserTeamCaptain(Long userId, Long teamId);
    
    // Операции для администрирования
    List<User> getAllUsersForAdmin();
    void suspendUser(Long userId, String reason);
    void unsuspendUser(Long userId);
    void banUser(Long userId, String reason);
    void unbanUser(Long userId);
    List<User> getSuspendedUsers();
    List<User> getBannedUsers();
    List<User> getInactiveUsers(int inactiveDays);
    
    // Операции с кэшированием
    void cacheUser(User user);
    void evictUserFromCache(Long userId);
    Optional<User> getCachedUser(Long userId);
    void cacheUserByUsername(String username, User user);
    void evictUserByUsernameFromCache(String username);
    Optional<User> getCachedUserByUsername(String username);
    
    // Операции с событиями
    void publishUserCreatedEvent(User user);
    void publishUserUpdatedEvent(User user);
    void publishUserDeletedEvent(User user);
    void publishUserActivatedEvent(User user);
    void publishUserDeactivatedEvent(User user);
    void publishUserRoleChangedEvent(User user, UserRole oldRole, UserRole newRole);
    
    // Интеграция с другими сервисами
    void notifyUser(User user, String message);
    void updateUserStatistics(User user);
    void syncUserWithExternalServices(User user);
    void validateUserDependencies(User user);
    
    // Аналитика и отчеты
    List<Object[]> getUserStatisticsByDay(Instant start, Instant end);
    List<Object[]> getUserActivityAnalysis(Long userId);
    List<Object[]> getUserPerformanceAnalysis(Long userId);
    List<Object[]> getUserGrowthMetrics();
    List<Object[]> getUserRetentionAnalysis();
    
    // Операции для оптимизации
    void batchCreateUsers(List<User> users);
    void batchUpdateUsers(List<User> users);
    List<User> getUsersForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateUserId();
    void logUserOperation(String operation, Long userId, Long operatorId);
    String generateUsername(String firstName, String lastName);
    boolean isValidPassword(String password);
    String hashPassword(String password);
    boolean verifyPassword(String password, String hashedPassword);
    
    // Операции с поиском
    Page<User> searchUsers(String keyword, Pageable pageable);
    List<User> getRecommendedUsers(Long userId, int limit);
    List<User> getSimilarUsers(Long userId, int limit);
    List<User> getUsersBySkillLevel(String skillLevel);
    List<User> getUsersByLocation(String location);
    
    // Операции с предпочтениями
    User updatePreferences(Long userId, Map<String, Object> preferences);
    Map<String, Object> getUserPreferences(Long userId);
    User setNotificationSettings(Long userId, Map<String, Boolean> settings);
    Map<String, Boolean> getNotificationSettings(Long userId);
    User setPrivacySettings(Long userId, Map<String, String> settings);
    Map<String, String> getPrivacySettings(Long userId);
    
    // Операции с друзьями
    void sendFriendRequest(Long fromUserId, Long toUserId);
    void acceptFriendRequest(Long fromUserId, Long toUserId);
    void rejectFriendRequest(Long fromUserId, Long toUserId);
    void removeFriend(Long userId, Long friendId);
    List<User> getUserFriends(Long userId);
    List<User> getPendingFriendRequests(Long userId);
    List<User> getSentFriendRequests(Long userId);
    boolean areFriends(Long userId1, Long userId2);
    
    // Операции с блокировкой
    void blockUser(Long userId, Long blockedUserId);
    void unblockUser(Long userId, Long blockedUserId);
    List<User> getBlockedUsers(Long userId);
    boolean isUserBlocked(Long userId, Long blockedUserId);
    
    // Операции с историей
    List<String> getUserHistory(Long userId);
    void addUserHistoryEntry(Long userId, String entry);
    List<String> getUserLoginHistory(Long userId);
    void recordUserLogin(Long userId, String ipAddress);
    List<String> getUserActionHistory(Long userId, Instant start, Instant end);
    
    // Операции с безопасностью
    User enableTwoFactorAuth(Long userId);
    User disableTwoFactorAuth(Long userId);
    String generateTwoFactorSecret(Long userId);
    boolean verifyTwoFactorCode(Long userId, String code);
    List<String> getSecurityQuestions(Long userId);
    User setSecurityQuestions(Long userId, Map<String, String> questions);
    boolean verifySecurityAnswer(Long userId, String question, String answer);
    
    // Операции с устройствами
    void registerDevice(Long userId, String deviceId, String deviceName);
    void unregisterDevice(Long userId, String deviceId);
    List<Map<String, Object>> getUserDevices(Long userId);
    void revokeAllDevices(Long userId);
    boolean isDeviceRegistered(Long userId, String deviceId);
    
    // Операции с сессиями авторизации
    List<String> getActiveTokens(Long userId);
    void revokeToken(Long userId, String tokenId);
    void revokeAllTokens(Long userId);
    void revokeExpiredTokens();
    boolean isTokenValid(Long userId, String tokenId);
    
    // Операции с экспортом
    Map<String, Object> exportUserData(Long userId);
    List<User> exportUsers();
    String generateUserReport(Long userId);
    String generateUsersReport();
    void importUserData(Map<String, Object> userData);
    
    // Операции с валидацией
    Map<String, List<String>> validateUserComplete(User user);
    List<String> getUserValidationErrors(Long userId);
    boolean isUserProfileComplete(Long userId);
    double getProfileCompleteness(Long userId);
    
    // Операции с метаданными
    User updateUserMetadata(Long userId, Map<String, Object> metadata);
    Map<String, Object> getUserMetadata(Long userId);
    void addUserMetadata(Long userId, String key, Object value);
    void removeUserMetadata(Long userId, String key);
    Object getUserMetadataValue(Long userId, String key);
    
    // Операции с мониторингом
    Map<String, Object> getUserHealthMetrics(Long userId);
    List<String> getUserErrors(Long userId);
    Map<String, Object> getUserPerformanceStats(Long userId);
    void monitorUserActivity(Long userId);
    List<Map<String, Object>> getUserActivityLog(Long userId, Instant start, Instant end);
    
    // Операции с аналитикой
    Map<String, Object> getUserAnalytics(Long userId);
    List<Object[]> getUserGameStats(Long userId);
    List<Object[]> getUserProgressStats(Long userId);
    List<Object[]> getUserSocialStats(Long userId);
    Map<String, Object> getUserEngagementMetrics(Long userId);
    
    // Операции с рекомендациями
    List<User> getRecommendedFriends(Long userId, int limit);
    List<Long> getRecommendedQuests(Long userId, int limit);
    List<Long> getRecommendedTeams(Long userId, int limit);
    Map<String, Object> getPersonalizedRecommendations(Long userId);
    
    // Операции с геймификацией
    User addExperiencePoints(Long userId, int points);
    User addLevel(Long userId);
    int getUserLevel(Long userId);
    long getUserExperiencePoints(Long userId);
    long getExperienceToNextLevel(Long userId);
    List<String> getUserUnlockedFeatures(Long userId);
    void unlockFeature(Long userId, String feature);
}