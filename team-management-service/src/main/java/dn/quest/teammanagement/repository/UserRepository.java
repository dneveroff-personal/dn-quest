package dn.quest.teammanagement.repository;

import dn.quest.teammanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по имени пользователя
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Найти пользователя по имени пользователя или email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Проверить, существует ли пользователь с таким именем
     */
    boolean existsByUsername(String username);

    /**
     * Проверить, существует ли пользователь с таким email
     */
    boolean existsByEmail(String email);

    /**
     * Найти активных пользователей
     */
    List<User> findByIsActiveTrue();

    /**
     * Найти пользователей по имени (с учетом регистра)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) AND u.isActive = true")
    Page<User> findByUsernameContainingIgnoreCaseAndIsActiveTrue(@Param("username") String username, Pageable pageable);

    /**
     * Найти пользователей по полному имени
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND u.isActive = true")
    Page<User> findByFullNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name, Pageable pageable);

    /**
     * Найти пользователей по имени пользователя или полному имени
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.isActive = true")
    Page<User> findByUsernameOrFullNameContainingIgnoreCaseAndIsActiveTrue(@Param("search") String search, Pageable pageable);

    /**
     * Найти пользователей по списку ID
     */
    @Query("SELECT u FROM User u WHERE u.id IN :userIds AND u.isActive = true")
    List<User> findByIdInAndIsActiveTrue(@Param("userIds") List<Long> userIds);

    /**
     * Получить количество активных пользователей
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Найти пользователей, зарегистрированных за определенный период
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate AND u.isActive = true")
    List<User> findUsersByRegistrationPeriod(@Param("startDate") java.time.Instant startDate, 
                                           @Param("endDate") java.time.Instant endDate);

    /**
     * Найти пользователей, которые не состоят в командах
     */
    @Query("SELECT u FROM User u " +
           "WHERE u.isActive = true " +
           "AND NOT EXISTS (SELECT 1 FROM TeamMember tm WHERE tm.user.id = u.id AND tm.isActive = true)")
    List<User> findUsersWithoutTeams();

    /**
     * Найти пользователей, которые состоят в определенном количестве команд
     */
    @Query("SELECT u FROM User u " +
           "WHERE u.isActive = true " +
           "AND (SELECT COUNT(tm) FROM TeamMember tm WHERE tm.user.id = u.id AND tm.isActive = true) = :teamCount")
    List<User> findUsersByTeamCount(@Param("teamCount") int teamCount);

    /**
     * Найти капитанов команд
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN Team t ON t.captain.id = u.id " +
           "WHERE u.isActive = true " +
           "AND t.isActive = true")
    List<User> findTeamCaptains();

    /**
     * Найти пользователей по первым буквам имени пользователя
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT(:prefix, '%')) AND u.isActive = true")
    List<User> findByUsernameStartingWithIgnoreCaseAndIsActiveTrue(@Param("prefix") String prefix);

    /**
     * Поиск пользователей для приглашений в команду (исключая текущих участников)
     */
    @Query("SELECT u FROM User u " +
           "WHERE u.isActive = true " +
           "AND u.id NOT IN (SELECT tm.user.id FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.isActive = true) " +
           "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findUsersForTeamInvitation(@Param("teamId") Long teamId, @Param("search") String search, Pageable pageable);
}