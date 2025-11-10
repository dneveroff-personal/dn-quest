package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.ParticipationRequest;
import dn.quest.gameengine.entity.Team;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с запросами на участие
 */
@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    // Базовые запросы
    List<ParticipationRequest> findBySession(GameSession session);
    List<ParticipationRequest> findByUser(User user);
    List<ParticipationRequest> findByTeam(Team team);
    List<ParticipationRequest> findByStatus(ParticipationStatus status);

    // Комбинированные запросы
    Optional<ParticipationRequest> findBySessionAndUser(GameSession session, User user);
    Optional<ParticipationRequest> findBySessionAndTeam(GameSession session, Team team);
    List<ParticipationRequest> findBySessionAndStatus(GameSession session, ParticipationStatus status);
    List<ParticipationRequest> findByUserAndStatus(User user, ParticipationStatus status);
    List<ParticipationRequest> findByTeamAndStatus(Team team, ParticipationStatus status);

    // Запросы для поиска активных запросов
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.status = 'PENDING' ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findPendingRequests();

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.status = 'PENDING' ORDER BY pr.createdAt DESC")
    Page<ParticipationRequest> findPendingRequests(Pageable pageable);

    // Запросы для поиска устаревших запросов
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.status = 'PENDING' AND pr.createdAt < :threshold ORDER BY pr.createdAt ASC")
    List<ParticipationRequest> findExpiredRequests(@Param("threshold") Instant threshold);

    // Запросы для поиска запросов пользователя
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.user = :user OR pr.team IN (SELECT tm.team FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true) ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findByUserOrTeam(@Param("user") User user);

    // Статистические запросы
    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.session = :session AND pr.status = :status")
    long countBySessionAndStatus(@Param("session") GameSession session, @Param("status") ParticipationStatus status);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.user = :user AND pr.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") ParticipationStatus status);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.team = :team AND pr.status = :status")
    long countByTeamAndStatus(@Param("team") Team team, @Param("status") ParticipationStatus status);

    // Запросы по времени
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.createdAt >= :since ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findByCreatedAtAfter(@Param("since") Instant since);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.processedAt >= :since ORDER BY pr.processedAt DESC")
    List<ParticipationRequest> findByProcessedAtAfter(@Param("since") Instant since);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.createdAt BETWEEN :start AND :end ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для анализа времени ожидания
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.status != 'PENDING' ORDER BY (pr.processedAt.getEpochSecond() - pr.createdAt.getEpochSecond()) DESC")
    List<ParticipationRequest> findLongestWaitingRequests();

    @Query("SELECT AVG(pr.processedAt.getEpochSecond() - pr.createdAt.getEpochSecond()) FROM ParticipationRequest pr WHERE pr.status != 'PENDING' AND pr.processedAt IS NOT NULL")
    Double getAverageWaitingTime();

    // Запросы для поиска запросов по обработчику
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.processedByUser = :user ORDER BY pr.processedAt DESC")
    List<ParticipationRequest> findByProcessedByUser(@Param("user") User user);

    // Запросы для анализа по IP
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.ipAddress = :ipAddress ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findByIpAddress(@Param("ipAddress") String ipAddress);

    // Запросы для поиска по кодам приглашений
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.invitationCode = :code")
    Optional<ParticipationRequest> findByInvitationCode(@Param("code") String code);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.invitationCode IS NOT NULL ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findRequestsWithInvitationCode();

    // Запросы для анализа типов запросов
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.user IS NOT NULL ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findUserRequests();

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.team IS NOT NULL ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findTeamRequests();

    // Запросы для статистики по типам
    @Query("SELECT pr.status, COUNT(pr) FROM ParticipationRequest pr GROUP BY pr.status")
    List<Object[]> getStatusStatistics();

    @Query("SELECT CASE WHEN pr.user IS NOT NULL THEN 'USER' WHEN pr.team IS NOT NULL THEN 'TEAM' ELSE 'UNKNOWN' END, COUNT(pr) FROM ParticipationRequest pr GROUP BY CASE WHEN pr.user IS NOT NULL THEN 'USER' WHEN pr.team IS NOT NULL THEN 'TEAM' ELSE 'UNKNOWN' END")
    List<Object[]> getTypeStatistics();

    // Запросы для поиска запросов с сообщениями
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.requestMessage IS NOT NULL AND pr.requestMessage != '' ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findRequestsWithMessage();

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.responseMessage IS NOT NULL AND pr.responseMessage != '' ORDER BY pr.processedAt DESC")
    List<ParticipationRequest> findRequestsWithResponse();

    // Запросы для пагинации
    Page<ParticipationRequest> findByStatusOrderByCreatedAtDesc(ParticipationStatus status, Pageable pageable);
    
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.user = :user ORDER BY pr.createdAt DESC")
    Page<ParticipationRequest> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    // Запросы для анализа активности
    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.createdAt >= :since")
    long countRequestsSince(@Param("since") Instant since);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.status = 'PENDING' AND pr.createdAt >= :since")
    long countPendingRequestsSince(@Param("since") Instant since);

    // Запросы для поиска дубликатов
    @Query("SELECT pr.user, pr.session, COUNT(pr) FROM ParticipationRequest pr WHERE pr.user IS NOT NULL GROUP BY pr.user, pr.session HAVING COUNT(pr) > 1")
    List<Object[]> findDuplicateUserRequests();

    @Query("SELECT pr.team, pr.session, COUNT(pr) FROM ParticipationRequest pr WHERE pr.team IS NOT NULL GROUP BY pr.team, pr.session HAVING COUNT(pr) > 1")
    List<Object[]> findDuplicateTeamRequests();

    // Запросы для анализа по квестам
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.session.level.quest.id = :questId ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findByQuestId(@Param("questId") Long questId);

    // Запросы для поиска запросов от конкретных пользователей
    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.user.id IN :userIds ORDER BY pr.createdAt DESC")
    List<ParticipationRequest> findByUserIds(@Param("userIds") List<Long> userIds);

    // Запросы для анализа эффективности обработки
    @Query("SELECT pr.processedByUser, COUNT(pr), AVG(pr.processedAt.getEpochSecond() - pr.createdAt.getEpochSecond()) FROM ParticipationRequest pr WHERE pr.processedByUser IS NOT NULL GROUP BY pr.processedByUser")
    List<Object[]> getProcessorStatistics();
}