package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    User updateProfile(UUID userId, String firstName, String lastName, String bio);
    User updateAvatar(UUID userId, String avatarUrl);
    User updateSettings(UUID userId, Map<String, Object> settings);
    User changePassword(UUID userId, String oldPassword, String newPassword);
    User deactivateUser(UUID userId);
    User activateUser(UUID userId);
    
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
    List<User> getHighestRatedUsers(int limit);
    
    // Игровая статистика
    User updateGameStats(UUID userId, int gamesPlayed, int gamesWon, long playtimeSeconds);
    Double getUserWinRate(UUID userId);
    Long getUserTotalPlaytime(UUID userId);
    int getUserGamesPlayed(UUID userId);
    int getUserGamesWon(UUID userId);
    List<User> getTopPlayersByScore(int limit);
    List<User> getTopPlayersByTime(int limit);
    List<User> getTopPlayersByWinRate(int limit);
    
    // Рейтинги и достижения
    User updateUserRating(UUID userId, Double newRating);
    Double calculateUserRating(UUID userId);
    Integer getUserRanking(UUID userId);
    List<String> getUserAchievements(UUID userId);
    void addAchievement(UUID userId, String achievementCode);
    void removeAchievement(UUID userId, String achievementCode);
    boolean hasAchievement(UUID userId, String achievementCode);
    
    // Валидация и бизнес-логика
    boolean canCreateUser(String username, String email);
    boolean canUpdateUser(UUID userId, Long editorId);
    boolean canDeleteUser(UUID userId, Long deleterId);
    boolean isValidUsername(String username);
    boolean isValidEmail(String email);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    
    // Управление ролями
    User assignRole(UUID userId, UserRole role);
    User removeRole(UUID userId, UserRole role);
    List<UserRole> getUserRoles(UUID userId);
    boolean hasRole(UUID userId, UserRole role);
    boolean hasAnyRole(UUID userId, List<UserRole> roles);
    
    // Управление сессиями
    List<Long> getUserActiveSessions(UUID userId);
    List<Long> getUserCompletedSessions(UUID userId);
    void addUserToSession(UUID userId, UUID sessionId);
    void removeUserFromSession(UUID userId, UUID sessionId);
    boolean isUserInSession(UUID userId, UUID sessionId);
    
    // Управление командами
    List<Long> getUserTeams(UUID userId);
    List<Long> getUserOwnedTeams(UUID userId);
    void addUserToTeam(UUID userId, UUID teamId);
    void removeUserFromTeam(UUID userId, UUID teamId);
    boolean isUserInTeam(UUID userId, UUID teamId);
    boolean isUserTeamCaptain(UUID userId, UUID teamId);
    
    // Операции для администрирования
    List<User> getAllUsersForAdmin();
    void suspendUser(UUID userId, String reason);
    void unsuspendUser(UUID userId);
    void banUser(UUID userId, String reason);
    void unbanUser(UUID userId);
    List<User> getSuspendedUsers();
    List<User> getBannedUsers();

    // Операции с кэшированием
    void cacheUser(User user);
    void evictUserFromCache(UUID userId);
    Optional<User> getCachedUser(UUID userId);
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
    List<Object[]> getUserActivityAnalysis(UUID userId);
    List<Object[]> getUserPerformanceAnalysis(UUID userId);
    List<Object[]> getUserGrowthMetrics();
    List<Object[]> getUserRetentionAnalysis();
    
    // Операции для оптимизации
    void batchCreateUsers(List<User> users);
    void batchUpdateUsers(List<User> users);
    List<User> getUsersForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateUserId();
    void logUserOperation(String operation, UUID userId, Long operatorId);
    String generateUsername(String firstName, String lastName);
    boolean isValidPassword(String password);
    String hashPassword(String password);
    boolean verifyPassword(String password, String hashedPassword);
    
    // Операции с поиском
    Page<User> searchUsers(String keyword, Pageable pageable);
    List<User> getRecommendedUsers(UUID userId, int limit);
    List<User> getSimilarUsers(UUID userId, int limit);
    List<User> getUsersBySkillLevel(String skillLevel);
    List<User> getUsersByLocation(String location);
    
    // Операции с предпочтениями
    User updatePreferences(UUID userId, Map<String, Object> preferences);
    Map<String, Object> getUserPreferences(UUID userId);
    User setNotificationSettings(UUID userId, Map<String, Boolean> settings);
    Map<String, Boolean> getNotificationSettings(UUID userId);
    User setPrivacySettings(UUID userId, Map<String, String> settings);
    Map<String, String> getPrivacySettings(UUID userId);
    
    // Операции с друзьями
    void sendFriendRequest(Long fromUserId, Long toUserId);
    void acceptFriendRequest(Long fromUserId, Long toUserId);
    void rejectFriendRequest(Long fromUserId, Long toUserId);
    void removeFriend(UUID userId, Long friendId);
    List<User> getUserFriends(UUID userId);
    List<User> getPendingFriendRequests(UUID userId);
    List<User> getSentFriendRequests(UUID userId);
    boolean areFriends(UUID userId1, UUID userId2);
    
    // Операции с блокировкой
    void blockUser(UUID userId, Long blockedUserId);
    void unblockUser(UUID userId, Long blockedUserId);
    List<User> getBlockedUsers(UUID userId);
    boolean isUserBlocked(UUID userId, Long blockedUserId);
    
    // Операции с историей
    List<String> getUserHistory(UUID userId);
    void addUserHistoryEntry(UUID userId, String entry);
    List<String> getUserLoginHistory(UUID userId);
    void recordUserLogin(UUID userId, String ipAddress);
    List<String> getUserActionHistory(UUID userId, Instant start, Instant end);
    
    // Операции с безопасностью
    User enableTwoFactorAuth(UUID userId);
    User disableTwoFactorAuth(UUID userId);
    String generateTwoFactorSecret(UUID userId);
    boolean verifyTwoFactorCode(UUID userId, String code);
    List<String> getSecurityQuestions(UUID userId);
    User setSecurityQuestions(UUID userId, Map<String, String> questions);
    boolean verifySecurityAnswer(UUID userId, String question, String answer);
    
    // Операции с устройствами
    void registerDevice(UUID userId, String deviceId, String deviceName);
    void unregisterDevice(UUID userId, String deviceId);
    List<Map<String, Object>> getUserDevices(UUID userId);
    void revokeAllDevices(UUID userId);
    boolean isDeviceRegistered(UUID userId, String deviceId);
    
    // Операции с сессиями авторизации
    List<String> getActiveTokens(UUID userId);
    void revokeToken(UUID userId, String tokenId);
    void revokeAllTokens(UUID userId);
    void revokeExpiredTokens();
    boolean isTokenValid(UUID userId, String tokenId);
    
    // Операции с экспортом
    Map<String, Object> exportUserData(UUID userId);
    List<User> exportUsers();
    String generateUserReport(UUID userId);
    String generateUsersReport();
    void importUserData(Map<String, Object> userData);
    
    // Операции с валидацией
    Map<String, List<String>> validateUserComplete(User user);
    List<String> getUserValidationErrors(UUID userId);
    boolean isUserProfileComplete(UUID userId);
    double getProfileCompleteness(UUID userId);
    
    // Операции с метаданными
    User updateUserMetadata(UUID userId, Map<String, Object> metadata);
    Map<String, Object> getUserMetadata(UUID userId);
    void addUserMetadata(UUID userId, String key, Object value);
    void removeUserMetadata(UUID userId, String key);
    Object getUserMetadataValue(UUID userId, String key);
    
    // Операции с мониторингом
    Map<String, Object> getUserHealthMetrics(UUID userId);
    List<String> getUserErrors(UUID userId);
    Map<String, Object> getUserPerformanceStats(UUID userId);
    void monitorUserActivity(UUID userId);
    List<Map<String, Object>> getUserActivityLog(UUID userId, Instant start, Instant end);
    
    // Операции с аналитикой
    Map<String, Object> getUserAnalytics(UUID userId);
    List<Object[]> getUserGameStats(UUID userId);
    List<Object[]> getUserProgressStats(UUID userId);
    List<Object[]> getUserSocialStats(UUID userId);
    Map<String, Object> getUserEngagementMetrics(UUID userId);
    
    // Операции с рекомендациями
    List<User> getRecommendedFriends(UUID userId, int limit);
    List<Long> getRecommendedQuests(UUID userId, int limit);
    List<Long> getRecommendedTeams(UUID userId, int limit);
    Map<String, Object> getPersonalizedRecommendations(UUID userId);
    
    // Операции с геймификацией
    User addExperiencePoints(UUID userId, int points);
    User addLevel(UUID userId);
    int getUserLevel(UUID userId);
    long getUserExperiencePoints(UUID userId);
    long getExperienceToNextLevel(UUID userId);
    List<String> getUserUnlockedFeatures(UUID userId);
    void unlockFeature(UUID userId, String feature);
}