package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.CodeAttempt;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.Level;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.AttemptResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с попытками ввода кодов
 */
@Repository
public interface CodeAttemptRepository extends JpaRepository<CodeAttempt, UUID> {

    // Базовые запросы
    List<CodeAttempt> findBySession(GameSession session);
    List<CodeAttempt> findByLevel(Level level);
    List<CodeAttempt> findByUser(User user);

    // Запросы с пагинацией
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session.id = :sessionId AND ca.level.id = :levelId ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findLastAttempts(@Param("sessionId") UUID sessionId, @Param("levelId") UUID levelId, Pageable pageable);

    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session.id = :sessionId AND ca.level.id = :levelId ORDER BY ca.createdAt DESC")
    Page<CodeAttempt> findAttemptsBySessionAndLevel(@Param("sessionId") UUID sessionId, @Param("levelId") UUID levelId, Pageable pageable);

    // Проверка дубликатов
    @Query("SELECT COUNT(ca) > 0 FROM CodeAttempt ca WHERE ca.session.id = :sessionId AND ca.level = :level AND ca.submittedNormalized = :normalized")
    boolean existsBySessionAndSubmittedNormalized(@Param("sessionId") UUID sessionId, @Param("level") Level level, @Param("normalized") String normalized);

    // Подсчет закрытых секторов
    @Query("SELECT COUNT(DISTINCT ca.matchedSectorNo) FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level AND ca.result IN ('ACCEPTED_NORMAL', 'ACCEPTED_BONUS', 'ACCEPTED_PENALTY')")
    long countDistinctClosedSectors(@Param("session") GameSession session, @Param("level") Level level);

    // Получение принятых попыток
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level AND ca.result IN :results ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findAcceptedAttempts(@Param("session") GameSession session, @Param("level") Level level, @Param("results") List<AttemptResult> results);

    // Запросы по результатам
    List<CodeAttempt> findByResult(AttemptResult result);
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.result = :result ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findBySessionAndResult(@Param("session") GameSession session, @Param("result") AttemptResult result);

    // Статистические запросы
    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level")
    long countBySessionAndLevel(@Param("session") GameSession session, @Param("level") Level level);

    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level AND ca.result = :result")
    long countBySessionAndLevelAndResult(@Param("session") GameSession session, @Param("level") Level level, @Param("result") AttemptResult result);

    // Запросы по времени
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.createdAt >= :since ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findByCreatedAtAfter(@Param("since") Instant since);

    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.createdAt BETWEEN :start AND :end ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для анализа активности
    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.session = :session AND ca.createdAt >= :since")
    long countAttemptsBySessionSince(@Param("session") GameSession session, @Param("since") Instant since);

    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.user = :user AND ca.createdAt >= :since")
    long countAttemptsByUserSince(@Param("user") User user, @Param("since") Instant since);

    // Запросы для мониторинга производительности
    @Query("SELECT AVG(ca.processingTimeMs) FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level")
    Double getAverageProcessingTime(@Param("session") GameSession session, @Param("level") Level level);

    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.processingTimeMs > :threshold ORDER BY ca.processingTimeMs DESC")
    List<CodeAttempt> findSlowAttempts(@Param("threshold") Long threshold);

    // Запросы для анализа по IP
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.ipAddress = :ipAddress ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findByIpAddress(@Param("ipAddress") String ipAddress);

    @Query("SELECT COUNT(DISTINCT ca.user) FROM CodeAttempt ca WHERE ca.ipAddress = :ipAddress AND ca.user IS NOT NULL")
    long countDistinctUsersByIpAddress(@Param("ipAddress") String ipAddress);

    // Запросы для поиска подозрительной активности
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.createdAt >= :since ORDER BY ca.createdAt ASC")
    List<CodeAttempt> findRecentAttemptsBySession(@Param("session") GameSession session, @Param("since") Instant since);

    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.session = :session AND ca.createdAt >= :since")
    long countRecentAttemptsBySession(@Param("session") GameSession session, @Param("since") Instant since);

    // Запросы для анализа успешности
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level AND ca.result IN ('ACCEPTED_NORMAL', 'ACCEPTED_BONUS', 'ACCEPTED_PENALTY') ORDER BY ca.createdAt ASC")
    List<CodeAttempt> findSuccessfulAttempts(@Param("session") GameSession session, @Param("level") Level level);

    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level AND ca.result = 'WRONG' ORDER BY ca.createdAt ASC")
    List<CodeAttempt> findWrongAttempts(@Param("session") GameSession session, @Param("level") Level level);

    // Запросы для анализа по пользователям
    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.user = :user AND ca.result = :result")
    long countByUserAndResult(@Param("user") User user, @Param("result") AttemptResult result);

    // Запросы для получения последней попытки
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.level = :level ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findLatestAttempts(@Param("session") GameSession session, @Param("level") Level level, Pageable pageable);

    // Запросы для анализа по кодам
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.matchedCode IS NOT NULL ORDER BY ca.createdAt DESC")
    List<CodeAttempt> findAttemptsWithMatchedCode();

    @Query("SELECT COUNT(ca) FROM CodeAttempt ca WHERE ca.matchedCode.id = :codeId")
    long countByMatchedCode(@Param("codeId") Long codeId);

    // Запросы для получения статистики по уровням
    @Query("SELECT ca.level.id, COUNT(ca), AVG(CASE WHEN ca.result IN ('ACCEPTED_NORMAL', 'ACCEPTED_BONUS', 'ACCEPTED_PENALTY') THEN 1 ELSE 0 END) " +
           "FROM CodeAttempt ca WHERE ca.session = :session GROUP BY ca.level.id")
    List<Object[]> getLevelStatistics(@Param("session") GameSession session);

    // Запросы для поиска дубликатов
    @Query("SELECT ca.submittedNormalized, COUNT(ca) FROM CodeAttempt ca WHERE ca.session = :session GROUP BY ca.submittedNormalized HAVING COUNT(ca) > 1")
    List<Object[]> findDuplicateAttempts(@Param("session") GameSession session);

    // Запросы для анализа временных паттернов
    @Query("SELECT ca FROM CodeAttempt ca WHERE ca.session = :session AND ca.createdAt BETWEEN :start AND :end ORDER BY ca.createdAt ASC")
    List<CodeAttempt> findAttemptsInTimeRange(@Param("session") GameSession session, @Param("start") Instant start, @Param("end") Instant end);

    // Запросы для получения самых активных пользователей
    @Query("SELECT ca.user, COUNT(ca) FROM CodeAttempt ca WHERE ca.createdAt >= :since GROUP BY ca.user ORDER BY COUNT(ca) DESC")
    List<Object[]> findMostActiveUsersSince(@Param("since") Instant since);
}