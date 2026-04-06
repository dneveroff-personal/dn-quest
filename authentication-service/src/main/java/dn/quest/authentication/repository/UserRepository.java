package dn.quest.authentication.repository;

import dn.quest.authentication.entity.User;
import dn.quest.shared.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    // Базовые запросы поиска
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByPublicNameContainingIgnoreCase(String name);
    Optional<User> findByUsernameAndIsActive(String username, Boolean isActive);

    // Запросы с JOIN FETCH для оптимизации
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userPermissions WHERE u.id = :id")
    Optional<User> findByIdWithPermissions(@Param("id") UUID id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userPermissions WHERE u.username = :username")
    Optional<User> findByUsernameWithPermissions(@Param("username") String username);

    // Поиск с пагинацией
    @Query("SELECT u FROM User u WHERE LOWER(u.publicName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByPublicNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findByRolePaged(@Param("role") UserRole role, Pageable pageable);

    // Запросы для токенов
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    // Запросы для статистики
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    // Запросы для управления пользователями
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.isActive = false")
    List<User> findInactiveUsers();

    // Запросы для email верификации
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false")
    List<User> findUsersWithUnverifiedEmail();

    // Запросы для очистки истекших токенов
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = NULL, u.refreshTokenExpiresAt = NULL WHERE u.refreshTokenExpiresAt < :date")
    int clearExpiredRefreshTokens(@Param("date") Instant date);

    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = NULL, u.passwordResetExpiresAt = NULL WHERE u.passwordResetExpiresAt < :date")
    int clearExpiredPasswordResetTokens(@Param("date") Instant date);

    // Поиск по нескольким ролям
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoleIn(@Param("roles") List<UserRole> roles);
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    Page<User> findByRoleInPaged(@Param("roles") List<UserRole> roles, Pageable pageable);

    // Проверка существования
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Поиск по дате регистрации
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Поиск по последнему входу
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :startDate AND u.lastLoginAt <= :endDate")
    List<User> findByLastLoginAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}