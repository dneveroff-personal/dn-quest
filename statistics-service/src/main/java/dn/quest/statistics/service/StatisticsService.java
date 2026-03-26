package dn.quest.statistics.service;

import java.time.Instant;

/**
 * Сервис для агрегации и обработки статистики
 */
public interface StatisticsService {

    /**
     * Обновление статистики регистрации пользователей
     */
    void updateUserRegistrationStatistics(Long userId, Instant timestamp);

    /**
     * Обновление статистики активности пользователей
     */
    void updateUserActivityStatistics(Long userId, Instant timestamp);

    /**
     * Обновление статистики удаления пользователей
     */
    void updateUserDeletionStatistics(Long userId, Instant timestamp);

    /**
     * Обновление статистики создания квестов
     */
    void updateQuestCreationStatistics(Long questId, Long authorId, Instant timestamp);

    /**
     * Обновление статистики обновления квестов
     */
    void updateQuestUpdateStatistics(Long questId, Instant timestamp);

    /**
     * Обновление статистики публикации квестов
     */
    void updateQuestPublicationStatistics(Long questId, Long authorId, Instant timestamp);

    /**
     * Обновление статистики удаления квестов
     */
    void updateQuestDeletionStatistics(Long questId, Instant timestamp);

    /**
     * Обновление статистики начала игровых сессий
     */
    void updateGameSessionStartStatistics(Long sessionId, Long userId, Long teamId, Long questId, Instant timestamp);

    /**
     * Обновление статистики завершения игровых сессий
     */
    void updateGameSessionFinishStatistics(Long sessionId, Long userId, Long teamId, Long questId, boolean completed, Instant timestamp);

    /**
     * Обновление статистики отправки кода
     */
    void updateCodeSubmissionStatistics(Long sessionId, Long userId, Long levelId, boolean success, Instant timestamp);

    /**
     * Обновление статистики завершения уровней
     */
    void updateLevelCompletionStatistics(Long sessionId, Long userId, Integer levelNumber, Long completionTime, Instant timestamp);

    /**
     * Обновление статистики создания команд
     */
    void updateTeamCreationStatistics(Long teamId, Long captainId, Instant timestamp);

    /**
     * Обновление статистики активности команд
     */
    void updateTeamActivityStatistics(Long teamId, Instant timestamp);

    /**
     * Обновление статистики членства в командах
     */
    void updateTeamMembershipStatistics(Long teamId, Long userId, String action, Instant timestamp);

    /**
     * Обновление статистики файлов
     */
    void updateFileStatistics(Long fileId, Long userId, Long fileSize, String action, Instant timestamp);
}