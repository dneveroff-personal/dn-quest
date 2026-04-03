package dn.quest.usermanagement.repository;

import dn.quest.shared.enums.UserRole;
import dn.quest.usermanagement.entity.UserProfile;
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
 * Repository для работы с профилями пользователей
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // Базовые запросы поиска
    Optional<UserProfile> findByUserId(UUID userId);
    Optional<UserProfile> findByUsername(String username);
    Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByRole(UserRole role);
    List<UserProfile> findByPublicNameContainingIgnoreCase(String publicName);

    // Запросы с пагинацией
    Page<UserProfile> findByPublicNameContainingIgnoreCase(String publicName, Pageable pageable);
    Page<UserProfile> findByRole(UserRole role, Pageable pageable);
    Page<UserProfile> findByIsActive(Boolean isActive, Pageable pageable);

    // Поиск активных/неактивных пользователей
    List<UserProfile> findByIsActive(Boolean isActive);
    List<UserProfile> findByIsBlocked(Boolean isBlocked);

    // Поиск заблокированных пользователей
    @Query("SELECT up FROM UserProfile up WHERE up.isBlocked = true AND (up.blockedUntil IS NULL OR up.blockedUntil > :now)")
    List<UserProfile> findCurrentlyBlocked(@Param("now") Instant now);

    @Query("SELECT up FROM UserProfile up WHERE up.isBlocked = true AND (up.blockedUntil IS NOT NULL AND up.blockedUntil <= :now)")
    List<UserProfile> findExpiredBlocks(@Param("now") Instant now);

    // Поиск по нескольким критериям
    @Query("SELECT up FROM UserProfile up WHERE " +
           "(:username IS NULL OR LOWER(up.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:publicName IS NULL OR LOWER(up.publicName) LIKE LOWER(CONCAT('%', :publicName, '%'))) AND " +
           "(:email IS NULL OR LOWER(up.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR up.role = :role) AND " +
           "(:isActive IS NULL OR up.isActive = :isActive)")
    Page<UserProfile> searchUsers(@Param("username") String username,
                                  @Param("publicName") String publicName,
                                  @Param("email") String email,
                                  @Param("role") UserRole role,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);

    // Статистические запросы
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.role = :role")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);

    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isBlocked = true AND (up.blockedUntil IS NULL OR up.blockedUntil > :now)")
    long countCurrentlyBlocked(@Param("now") Instant now);

    // Запросы для активности
    @Query("SELECT up FROM UserProfile up WHERE up.lastActivityAt >= :since")
    List<UserProfile> findActiveSince(@Param("since") Instant since);

    @Query("SELECT up FROM UserProfile up WHERE up.lastActivityAt < :before")
    List<UserProfile> findInactiveSince(@Param("before") Instant before);

    // Запросы для лидербордов
    @Query("SELECT up FROM UserProfile up ORDER BY up.lastActivityAt DESC")
    Page<UserProfile> findMostRecentlyActive(Pageable pageable);

    // Запросы для администрирования
    @Query("SELECT up FROM UserProfile up WHERE up.createdAt >= :since")
    List<UserProfile> findRecentlyRegistered(@Param("since") Instant since);

    @Query("SELECT up FROM UserProfile up WHERE up.createdAt BETWEEN :from AND :to")
    List<UserProfile> findRegisteredBetween(@Param("from") Instant from, @Param("to") Instant to);

    // Проверка существования
    boolean existsByUserId(UUID userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Массовые операции
    @Query("UPDATE UserProfile up SET up.isBlocked = false, up.blockedUntil = null, up.blockReason = null WHERE up.id IN :ids")
    void unblockUsers(@Param("ids") List<Long> ids);

    @Query("UPDATE UserProfile up SET up.isActive = false WHERE up.id IN :ids")
    void deactivateUsers(@Param("ids") List<Long> ids);

    @Query("UPDATE UserProfile up SET up.isActive = true WHERE up.id IN :ids")
    void activateUsers(@Param("ids") List<Long> ids);
}