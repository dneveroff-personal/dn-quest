package dn.quest.statistics.service;

import java.time.Instant;
import java.util.UUID;

/**
 * Сервис для агрегации и обработки статистики
 */
public interface StatisticsService {

    /**
     * Обновление статистики регистрации пользователей
     */
    void updateUserRegistrationStatistics(UUID userId, Instant timestamp);

    /**
     * Обновление статистики активности пользователей
     */
    void updateUserActivityStatistics(UUID userId, Instant timestamp);

    /**
     * Обновление статистики удаления пользователей
     */
    void updateUserDeletionStatistics(UUID userId, Instant timestamp);

    /**
     * Обновление статистики создания квестов
     */
    void updateQuestCreationStatistics(UUID questId, UUID authorId, Instant timestamp);

    /**
     * Обновление статистики обновления квестов
     */
    void updateQuestUpdateStatistics(UUID questId, Instant timestamp);

    /**
     * Обновление статистики публикации квестов
     */
    void updateQuestPublicationStatistics(UUID questId, UUID authorId, Instant timestamp);

    /**
     * Обновление статистики удаления квестов
     */
    void updateQuestDeletionStatistics(UUID questId, Instant timestamp);

    /**
     * Обновление статистики начала игровых сессий
     */
    void updateGameSessionStartStatistics(UUID sessionId, UUID userId, UUID teamId, UUID questId, Instant timestamp);

    /**
     * Обновление статистики завершения игровых сессий
     */
    void updateGameSessionFinishStatistics(UUID sessionId, UUID userId, UUID teamId, UUID questId, boolean completed, Instant timestamp);

    /**
     * Обновление статистики отправки кода
     */
    void updateCodeSubmissionStatistics(UUID sessionId, UUID userId, UUID levelId, boolean success, Instant timestamp);

    /**
     * Обновление статистики завершения уровней
     */
    void updateLevelCompletionStatistics(UUID sessionId, UUID userId, Integer levelNumber, Long completionTime, Instant timestamp);

    /**
     * Обновление статистики создания команд
     */
    void updateTeamCreationStatistics(UUID teamId, UUID captainId, Instant timestamp);

    /**
     * Обновление статистики активности команд
     */
    void updateTeamActivityStatistics(UUID teamId, Instant timestamp);

    /**
     * Обновление статистики членства в командах
     */
    void updateTeamMembershipStatistics(UUID teamId, UUID userId, String action, Instant timestamp);

    /**
     * Обновление статистики файлов
     */
    void updateFileStatistics(Long fileId, UUID userId, Long fileSize, String action, Instant timestamp);
}