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
    void updateQuestCreationStatistics(Long questId, UUID authorId, Instant timestamp);

    /**
     * Обновление статистики обновления квестов
     */
    void updateQuestUpdateStatistics(Long questId, Instant timestamp);

    /**
     * Обновление статистики публикации квестов
     */
    void updateQuestPublicationStatistics(Long questId, UUID authorId, Instant timestamp);

    /**
     * Обновление статистики удаления квестов
     */
    void updateQuestDeletionStatistics(Long questId, Instant timestamp);

    /**
     * Обновление статистики начала игровых сессий
     */
    void updateGameSessionStartStatistics(Long sessionId, UUID userId, UUID teamId, Long questId, Instant timestamp);

    /**
     * Обновление статистики завершения игровых сессий
     */
    void updateGameSessionFinishStatistics(Long sessionId, UUID userId, UUID teamId, Long questId, boolean completed, Instant timestamp);

    /**
     * Обновление статистики отправки кода
     */
    void updateCodeSubmissionStatistics(Long sessionId, UUID userId, Long levelId, boolean success, Instant timestamp);

    /**
     * Обновление статистики завершения уровней
     */
    void updateLevelCompletionStatistics(Long sessionId, UUID userId, Integer levelNumber, Long completionTime, Instant timestamp);

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