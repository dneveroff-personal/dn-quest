package dn.quest.authentication.repository;

import dn.quest.authentication.entity.UserPermission;
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
 * Repository для работы с разрешениями пользователей
 */
@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID>, JpaSpecificationExecutor<UserPermission> {

    // Поиск по пользователю
    List<UserPermission> findByUserId(UUID userId);
    Optional<UserPermission> findByUserIdAndPermissionId(UUID userId, UUID permissionId);

    // Поиск с JOIN FETCH для оптимизации
    @Query("SELECT up FROM UserPermission up LEFT JOIN FETCH up.user LEFT JOIN FETCH up.permission WHERE up.user.id = :userId")
    List<UserPermission> findByUserIdWithPermissions(@Param("userId") UUID userId);

    // Поиск по разрешению
    List<UserPermission> findByPermissionId(UUID permissionId);

    // Поиск по имени пользователя
    @Query("SELECT up FROM UserPermission up JOIN up.user u WHERE u.username = :username")
    List<UserPermission> findByUsername(@Param("username") String username);

    // Поиск по имени разрешения
    @Query("SELECT up FROM UserPermission up JOIN up.permission p WHERE p.name = :permissionName")
    List<UserPermission> findByPermissionName(@Param("permissionName") String permissionName);

    // Проверка наличия разрешения у пользователя
    @Query("SELECT COUNT(up) > 0 FROM UserPermission up JOIN up.user u JOIN up.permission p WHERE u.id = :userId AND p.name = :permissionName")
    boolean hasUserPermission(@Param("userId") UUID userId, @Param("permissionName") String permissionName);

    // Проверка наличия разрешения у пользователя по username
    @Query("SELECT COUNT(up) > 0 FROM UserPermission up JOIN up.user u JOIN up.permission p WHERE u.username = :username AND p.name = :permissionName")
    boolean hasUserPermissionByUsername(@Param("username") String username, @Param("permissionName") String permissionName);

    // Получение всех разрешений пользователя
    @Query("SELECT p.name FROM UserPermission up JOIN up.user u JOIN up.permission p WHERE u.id = :userId")
    List<String> findPermissionNamesByUserId(@Param("userId") UUID userId);

    // Получение всех разрешений пользователя по username
    @Query("SELECT p.name FROM UserPermission up JOIN up.user u JOIN up.permission p WHERE u.username = :username")
    List<String> findPermissionNamesByUsername(@Param("username") String username);

    // Удаление разрешений пользователя
    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.user.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.user.id = :userId AND up.permission.id = :permissionId")
    int deleteByUserIdAndPermissionId(@Param("userId") UUID userId, @Param("permissionId") UUID permissionId);

    // Удаление разрешений по имени
    @Modifying
    @Query("DELETE FROM UserPermission up WHERE up.user.id = :userId AND up.permission.name = :permissionName")
    int deleteByUserIdAndPermissionName(@Param("userId") UUID userId, @Param("permissionName") String permissionName);

    // Поиск по дате выдачи
    @Query("SELECT up FROM UserPermission up WHERE up.grantedAt >= :startDate AND up.grantedAt <= :endDate")
    List<UserPermission> findByGrantedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Поиск по выдавшему
    List<UserPermission> findByGrantedBy(String grantedBy);

    // Статистика
    @Query("SELECT COUNT(up) FROM UserPermission up WHERE up.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(up) FROM UserPermission up WHERE up.permission.id = :permissionId")
    long countByPermissionId(@Param("permissionId") UUID permissionId);

    // Получение пользователей с определенным разрешением
    @Query("SELECT u.id FROM UserPermission up JOIN up.user u JOIN up.permission p WHERE p.name = :permissionName")
    List<Long> findUserIdsWithPermission(@Param("permissionName") String permissionName);
}