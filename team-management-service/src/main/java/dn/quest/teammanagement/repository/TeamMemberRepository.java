package dn.quest.teammanagement.repository;

import dn.quest.shared.enums.TeamRole;
import dn.quest.teammanagement.entity.Team;
import dn.quest.teammanagement.entity.TeamMember;
import dn.quest.teammanagement.entity.User;
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
 * Репозиторий для работы с участниками команд
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    /**
     * Найти участников команды
     */
    List<TeamMember> findByTeam(Team team);

    /**
     * Найти активных участников команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true ORDER BY tm.role DESC, tm.joinedAt ASC")
    List<TeamMember> findByTeamAndIsActiveTrueOrderByRoleDescJoinedAtAsc(@Param("team") Team team);

    /**
     * Найти участника по пользователю
     */
    Optional<TeamMember> findByUser(User user);

    /**
     * Найти активного участника по пользователю
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true")
    Optional<TeamMember> findByUserAndIsActiveTrue(@Param("user") User user);

    /**
     * Найти участника по команде и пользователю
     */
    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    /**
     * Найти активного участника по команде и пользователю
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.user = :user AND tm.isActive = true")
    Optional<TeamMember> findByTeamAndUserAndIsActiveTrue(@Param("team") Team team, @Param("user") User user);

    /**
     * Найти участников по роли
     */
    List<TeamMember> findByRole(TeamRole role);

    /**
     * Найти участников команды по роли
     */
    List<TeamMember> findByTeamAndRole(Team team, TeamRole role);

    /**
     * Найти активных участников команды по роли
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.role = :role AND tm.isActive = true")
    List<TeamMember> findByTeamAndRoleAndIsActiveTrue(@Param("team") Team team, @Param("role") TeamRole role);

    /**
     * Найти капитана команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.role = :role AND tm.isActive = true")
    Optional<TeamMember> findCaptainByTeamAndRole(@Param("team") Team team, @Param("role") TeamRole role);

    /**
     * Найти все команды пользователя
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true")
    List<TeamMember> findTeamsByUser(@Param("user") User user);

    /**
     * Найти команды пользователя с определенной ролью
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user = :user AND tm.role = :role AND tm.isActive = true")
    List<TeamMember> findByUserAndRoleAndIsActiveTrue(@Param("user") User user, @Param("role") TeamRole role);

    /**
     * Проверить, состоит ли пользователь в команде
     */
    boolean existsByTeamAndUserAndIsActiveTrue(Team team, User user);

    /**
     * Проверить, является ли пользователь капитаном команды
     */
    boolean existsByTeamAndUserAndRoleAndIsActiveTrue(Team team, User user, TeamRole role);

    /**
     * Получить количество участников в команде
     */
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true")
    long countByTeamAndIsActiveTrue(@Param("team") Team team);

    /**
     * Получить количество участников с определенной ролью в команде
     */
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.role = :role AND tm.isActive = true")
    long countByTeamAndRoleAndIsActiveTrue(@Param("team") Team team, @Param("role") TeamRole role);

    /**
     * Получить количество команд пользователя
     */
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true")
    long countByUserAndIsActiveTrue(@Param("user") User user);

    /**
     * Найти участников, присоединившихся за определенный период
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.joinedAt BETWEEN :startDate AND :endDate AND tm.isActive = true")
    List<TeamMember> findByJoinedAtBetweenAndIsActiveTrue(@Param("startDate") Instant startDate, 
                                                         @Param("endDate") Instant endDate);

    /**
     * Найти участников, покинувших команду за определенный период
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.leftAt BETWEEN :startDate AND :endDate")
    List<TeamMember> findByLeftAtBetween(@Param("startDate") Instant startDate, 
                                        @Param("endDate") Instant endDate);

    /**
     * Найти самых старых участников команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findByTeamAndIsActiveTrueOrderByJoinedAtAsc(@Param("team") Team team);

    /**
     * Найти самых новых участников команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true ORDER BY tm.joinedAt DESC")
    List<TeamMember> findByTeamAndIsActiveTrueOrderByJoinedAtDesc(@Param("team") Team team);

    /**
     * Найти участников для пагинации
     */
    Page<TeamMember> findActiveMembersByTeam(@Param("team") Team team, Pageable pageable);

    /**
     * Найти всех неактивных участников
     */
    List<TeamMember> findByIsActiveFalse();

    /**
     * Найти неактивных участников команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = false")
    List<TeamMember> findByTeamAndIsActiveFalse(@Param("team") Team team);

    /**
     * Удалить всех неактивных участников старше определенной даты
     */
    @Query("DELETE FROM TeamMember tm WHERE tm.isActive = false AND tm.leftAt < :date")
    void deleteInactiveMembersOlderThan(@Param("date") Instant date);

    /**
     * Найти участников без активной команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.isActive = false")
    List<TeamMember> findActiveMembersWithInactiveTeams();

    /**
     * Получить статистику по ролям в команде
     */
    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true GROUP BY tm.role")
    List<Object[]> countMembersByRoleInTeam(@Param("team") Team team);
}