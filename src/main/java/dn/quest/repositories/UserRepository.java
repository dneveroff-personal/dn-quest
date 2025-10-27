package dn.quest.repositories;

import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Базовые запросы поиска
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByPublicNameContainingIgnoreCase(String name);

    // Оптимизированные запросы с JOIN FETCH
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.teamMembers WHERE u.id = :id")
    Optional<User> findByIdWithTeamMembers(@Param("id") Long id);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.teamMembers WHERE u.username = :username")
    Optional<User> findByUsernameWithTeamMembers(@Param("username") String username);
    
    // Поиск с пагинацией
    @Query("SELECT u FROM User u WHERE LOWER(u.publicName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByPublicNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findByRolePaged(@Param("role") UserRole role, Pageable pageable);
    
    // Запросы для команд
    @Query("SELECT u FROM User u JOIN u.teamMembers tm WHERE tm.team.id = :teamId")
    List<User> findByTeamId(@Param("teamId") Long teamId);
    
    @Query("SELECT u FROM User u JOIN u.teamMembers tm WHERE tm.team.id = :teamId AND tm.role = 'CAPTAIN'")
    Optional<User> findCaptainByTeamId(@Param("teamId") Long teamId);
    
    // Запросы для статистики
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
    
    // Поиск пользователей без команды
    @Query("SELECT u FROM User u LEFT JOIN u.teamMembers tm WHERE tm IS NULL")
    List<User> findUsersWithoutTeam();
    
    // Поиск активных пользователей (с командой)
    @Query("SELECT DISTINCT u FROM User u JOIN u.teamMembers tm")
    List<User> findUsersWithTeam();
    
    // Поиск по нескольким ролям
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoleIn(@Param("roles") List<UserRole> roles);
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    Page<User> findByRoleInPaged(@Param("roles") List<UserRole> roles, Pageable pageable);
    
    // Поиск пользователей для приглашений
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT tm.user.id FROM TeamMember tm WHERE tm.team.id = :teamId) AND u.id != :excludeUserId")
    List<User> findUsersForInvitation(@Param("teamId") Long teamId, @Param("excludeUserId") Long excludeUserId);

}
