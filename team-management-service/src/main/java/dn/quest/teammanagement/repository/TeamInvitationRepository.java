package dn.quest.teammanagement.repository;

import dn.quest.shared.enums.InvitationStatus;
import dn.quest.teammanagement.entity.Team;
import dn.quest.teammanagement.entity.TeamInvitation;
import dn.quest.teammanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с приглашениями в команды
 */
@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {

    /**
     * Найти приглашения пользователя
     */
    List<TeamInvitation> findByUser(User user);

    /**
     * Найти приглашения пользователя по статусу
     */
    List<TeamInvitation> findByUserAndStatus(User user, InvitationStatus status);

    /**
     * Найти активные приглашения пользователя
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.user = :user AND ti.status = 'PENDING' AND (ti.expiresAt IS NULL OR ti.expiresAt > :now)")
    List<TeamInvitation> findActiveInvitationsByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Найти приглашения команды
     */
    List<TeamInvitation> findByTeam(Team team);

    /**
     * Найти приглашения команды по статусу
     */
    List<TeamInvitation> findByTeamAndStatus(Team team, InvitationStatus status);

    /**
     * Найти активные приглашения команды
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.team = :team AND ti.status = 'PENDING' AND (ti.expiresAt IS NULL OR ti.expiresAt > :now)")
    List<TeamInvitation> findActiveInvitationsByTeam(@Param("team") Team team, @Param("now") Instant now);

    /**
     * Найти приглашение по команде и пользователю
     */
    Optional<TeamInvitation> findByTeamAndUser(Team team, User user);

    /**
     * Найти приглашение по команде, пользователю и статусу
     */
    Optional<TeamInvitation> findByTeamAndUserAndStatus(Team team, User user, InvitationStatus status);

    /**
     * Найти активное приглашение по команде и пользователю
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.team = :team AND ti.user = :user AND ti.status = 'PENDING' AND (ti.expiresAt IS NULL OR ti.expiresAt > :now)")
    Optional<TeamInvitation> findActiveInvitationByTeamAndUser(@Param("team") Team team, @Param("user") User user, @Param("now") Instant now);

    /**
     * Найти приглашения, отправленные пользователем
     */
    List<TeamInvitation> findByInvitedBy(User invitedBy);

    /**
     * Найти приглашения, отправленные пользователем, по статусу
     */
    List<TeamInvitation> findByInvitedByAndStatus(User invitedBy, InvitationStatus status);

    /**
     * Проверить, существует ли активное приглашение
     */
    @Query("SELECT COUNT(ti) > 0 FROM TeamInvitation ti WHERE ti.team = :team AND ti.user = :user AND ti.status = 'PENDING' AND (ti.expiresAt IS NULL OR ti.expiresAt > :now)")
    boolean existsActiveInvitation(@Param("team") Team team, @Param("user") User user, @Param("now") Instant now);

    /**
     * Проверить, существует ли приглашение с определенным статусом
     */
    boolean existsByTeamAndUserAndStatus(Team team, User user, InvitationStatus status);

    /**
     * Получить количество приглашений пользователя по статусу
     */
    @Query("SELECT COUNT(ti) FROM TeamInvitation ti WHERE ti.user = :user AND ti.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") InvitationStatus status);

    /**
     * Получить количество активных приглашений пользователя
     */
    @Query("SELECT COUNT(ti) FROM TeamInvitation ti WHERE ti.user = :user AND ti.status = 'PENDING' AND (ti.expiresAt IS NULL OR ti.expiresAt > :now)")
    long countActiveInvitationsByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Получить количество приглашений команды по статусу
     */
    @Query("SELECT COUNT(ti) FROM TeamInvitation ti WHERE ti.team = :team AND ti.status = :status")
    long countByTeamAndStatus(@Param("team") Team team, @Param("status") InvitationStatus status);

    /**
     * Получить количество активных приглашений команды
     */
    @Query("SELECT COUNT(ti) FROM TeamInvitation ti WHERE ti.team = :team AND ti.status = 'PENDING' AND (ti.expiresAt IS NULL OR ti.expiresAt > :now)")
    long countActiveInvitationsByTeam(@Param("team") Team team, @Param("now") Instant now);

    /**
     * Найти приглашения, созданные за определенный период
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.createdAt BETWEEN :startDate AND :endDate")
    List<TeamInvitation> findByCreatedAtBetween(@Param("startDate") Instant startDate, 
                                               @Param("endDate") Instant endDate);

    /**
     * Найти приглашения, ответ на которые был получен за определенный период
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.respondedAt BETWEEN :startDate AND :endDate")
    List<TeamInvitation> findByRespondedAtBetween(@Param("startDate") Instant startDate, 
                                                 @Param("endDate") Instant endDate);

    /**
     * Найти истекшие приглашения
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.status = 'PENDING' AND ti.expiresAt IS NOT NULL AND ti.expiresAt <= :now")
    List<TeamInvitation> findExpiredInvitations(@Param("now") Instant now);

    /**
     * Найти приглашения для пагинации
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.user = :user ORDER BY ti.createdAt DESC")
    Page<TeamInvitation> findByUser(@Param("user") User user, Pageable pageable);

    /**
     * Найти приглашения команды для пагинации
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.team = :team ORDER BY ti.createdAt DESC")
    Page<TeamInvitation> findByTeam(@Param("team") Team team, Pageable pageable);

    /**
     * Найти приглашения по статусу для пагинации
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.status = :status ORDER BY ti.createdAt DESC")
    Page<TeamInvitation> findByStatus(@Param("status") InvitationStatus status, Pageable pageable);

    /**
     * Обновить статус истекших приглашений
     */
    @Modifying
    @Query("UPDATE TeamInvitation ti SET ti.status = 'EXPIRED', ti.respondedAt = :now WHERE ti.status = 'PENDING' AND ti.expiresAt IS NOT NULL AND ti.expiresAt <= :now")
    int updateExpiredInvitations(@Param("now") Instant now);

    /**
     * Удалить старые приглашения (старше определенного периода)
     */
    @Modifying
    @Query("DELETE FROM TeamInvitation ti WHERE ti.createdAt < :date")
    int deleteInvitationsOlderThan(@Param("date") Instant date);

    /**
     * Получить статистику по приглашениям
     */
    @Query("SELECT ti.status, COUNT(ti) FROM TeamInvitation ti GROUP BY ti.status")
    List<Object[]> countInvitationsByStatus();

    /**
     * Получить статистику по приглашениям команды
     */
    @Query("SELECT ti.status, COUNT(ti) FROM TeamInvitation ti WHERE ti.team = :team GROUP BY ti.status")
    List<Object[]> countInvitationsByStatusForTeam(@Param("team") Team team);

    /**
     * Найти приглашения с определенным сообщением
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.invitationMessage IS NOT NULL AND LOWER(ti.invitationMessage) LIKE LOWER(CONCAT('%', :message, '%'))")
    List<TeamInvitation> findByInvitationMessageContainingIgnoreCase(@Param("message") String message);

    /**
     * Найти последние приглашения пользователя
     */
    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.user = :user ORDER BY ti.createdAt DESC")
    List<TeamInvitation> findRecentInvitationsByUser(@Param("user") User user, Pageable pageable);
}