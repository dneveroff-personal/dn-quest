package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Базовые запросы
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Запросы для поиска активных пользователей
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findActiveUsers();

    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    Page<User> findActiveUsers(Pageable pageable);

    // Запросы для поиска верифицированных пользователей
    @Query("SELECT u FROM User u WHERE u.isVerified = true ORDER BY u.createdAt DESC")
    List<User> findVerifiedUsers();

    // Запросы для поиска по ролям
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true ORDER BY u.publicName ASC")
    List<User> findActiveByRole(@Param("role") UserRole role);

    // Запросы для поиска по имени
    @Query("SELECT u FROM User u WHERE LOWER(u.publicName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY u.publicName ASC")
    List<User> findByPublicNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) ORDER BY u.username ASC")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);

    // Запросы для поиска по рейтингу
    @Query("SELECT u FROM User u WHERE u.rating >= :minRating ORDER BY u.rating DESC")
    List<User> findByRatingGreaterThanEqual(@Param("minRating") Double minRating);

    @Query("SELECT u FROM User u WHERE u.rating BETWEEN :minRating AND :maxRating ORDER BY u.rating DESC")
    List<User> findByRatingBetween(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating);

    @Query("SELECT u FROM User u ORDER BY u.rating DESC")
    List<User> findTopUsersByRating(Pageable pageable);

    // Запросы для поиска по статистике игр
    @Query("SELECT u FROM User u WHERE u.totalGamesPlayed > 0 ORDER BY u.totalGamesPlayed DESC")
    List<User> findMostActivePlayers();

    @Query("SELECT u FROM User u WHERE u.totalGamesWon > 0 ORDER BY u.totalGamesWon DESC")
    List<User> findTopWinners();

    @Query("SELECT u FROM User u WHERE u.totalPlaytimeSeconds > 0 ORDER BY u.totalPlaytimeSeconds DESC")
    List<User> findMostExperiencedPlayers();

    // Запросы для поиска по времени
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyActiveUsers(@Param("since") Instant since);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegisteredUsers(@Param("since") Instant since);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findUsersWhoNeverLoggedIn();

    // Статистические запросы
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isVerified = true")
    long countVerifiedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT AVG(u.rating) FROM User u WHERE u.rating IS NOT NULL")
    Double getAverageRating();

    @Query("SELECT AVG(u.totalGamesPlayed) FROM User u WHERE u.totalGamesPlayed > 0")
    Double getAverageGamesPlayed();

    // Запросы для анализа по ролям
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserRoleStatistics();

    @Query("SELECT u.role, AVG(u.rating), AVG(u.totalGamesPlayed) FROM User u WHERE u.rating IS NOT NULL GROUP BY u.role")
    List<Object[]> getUserRoleStatisticsWithRating();

    // Запросы для поиска авторов квестов
    @Query("SELECT DISTINCT u FROM User u JOIN u.authoredQuests q WHERE q.published = true ORDER BY u.publicName ASC")
    List<User> findQuestAuthors();

    @Query("SELECT DISTINCT u FROM User u JOIN u.authoredQuests q WHERE q.published = true AND u.isActive = true ORDER BY u.publicName ASC")
    List<User> findActiveQuestAuthors();

    // Запросы для поиска капитанов команд
    @Query("SELECT DISTINCT u FROM User u JOIN u.captainedTeams t WHERE t.isActive = true ORDER BY u.publicName ASC")
    List<User> findTeamCaptains();

    // Запросы для поиска участников команд
    @Query("SELECT DISTINCT u FROM User u JOIN u.teamMemberships tm WHERE tm.isActive = true ORDER BY u.publicName ASC")
    List<User> findTeamMembers();

    // Запросы для поиска пользователей с определенными характеристиками
    @Query("SELECT u FROM User u WHERE u.totalGamesPlayed >= :minGames ORDER BY u.totalGamesPlayed DESC")
    List<User> findExperiencedPlayers(@Param("minGames") Integer minGames);

    @Query("SELECT u FROM User u WHERE u.totalPlaytimeSeconds >= :minPlaytime ORDER BY u.totalPlaytimeSeconds DESC")
    List<User> findPlayersWithMinPlaytime(@Param("minPlaytime") Long minPlaytime);

    // Запросы для поиска по проценту побед
    @Query("SELECT u FROM User u WHERE u.totalGamesPlayed > 0 ORDER BY (CAST(u.totalGamesWon AS double) / u.totalGamesPlayed) DESC")
    List<User> findPlayersByWinRatio();

    @Query("SELECT u FROM User u WHERE u.totalGamesPlayed > 0 AND (CAST(u.totalGamesWon AS double) / u.totalGamesPlayed) >= :minRatio ORDER BY (CAST(u.totalGamesWon AS double) / u.totalGamesPlayed) DESC")
    List<User> findPlayersWithMinWinRatio(@Param("minRatio") Double minRatio);

    // Запросы для комплексного поиска
    @Query("""
        SELECT u FROM User u 
        WHERE (:isActive IS NULL OR u.isActive = :isActive)
          AND (:isVerified IS NULL OR u.isVerified = :isVerified)
          AND (:role IS NULL OR u.role = :role)
          AND (:minRating IS NULL OR u.rating >= :minRating)
          AND (:maxRating IS NULL OR u.rating <= :maxRating)
          AND (:minGamesPlayed IS NULL OR u.totalGamesPlayed >= :minGamesPlayed)
          AND (:name IS NULL OR LOWER(u.publicName) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY u.rating DESC
    """)
    List<User> findUsersWithFilters(
        @Param("isActive") Boolean isActive,
        @Param("isVerified") Boolean isVerified,
        @Param("role") UserRole role,
        @Param("minRating") Double minRating,
        @Param("maxRating") Double maxRating,
        @Param("minGamesPlayed") Integer minGamesPlayed,
        @Param("name") String name
    );

    // Запросы для анализа активности
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since AND u.isActive = true ORDER BY u.lastLoginAt DESC")
    List<User> findActiveUsersSince(@Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :since")
    long countActiveUsersSince(@Param("since") Instant since);

    // Запросы для поиска неактивных пользователей
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :threshold AND u.isActive = true ORDER BY u.lastLoginAt ASC")
    List<User> findInactiveUsers(@Param("threshold") Instant threshold);

    // Запросы для поиска пользователей с аватарами
    @Query("SELECT u FROM User u WHERE u.avatarUrl IS NOT NULL AND u.avatarUrl != '' ORDER BY u.createdAt DESC")
    List<User> findUsersWithAvatars();

    // Запросы для поиска по номеру телефона
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Запросы для пагинации
    Page<User> findByIsActiveTrueOrderByPublicNameAsc(Pageable pageable);
    Page<User> findByRoleOrderByPublicNameAsc(UserRole role, Pageable pageable);

    // Запросы для анализа по времени регистрации
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :start AND :end ORDER BY u.createdAt DESC")
    List<User> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для поиска лучших игроков по разным метрикам
    @Query("SELECT u FROM User u WHERE u.totalGamesPlayed >= 10 ORDER BY (CAST(u.totalGamesWon AS double) / u.totalGamesPlayed) DESC, u.totalGamesWon DESC")
    List<User> findBestPlayers();

    @Query("SELECT u FROM User u WHERE u.totalPlaytimeSeconds >= 3600 ORDER BY u.rating DESC")
    List<User> findExperiencedHighRatedPlayers();
}