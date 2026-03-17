package dn.quest.notification.service;

import dn.quest.shared.events.notification.NotificationEvent;

/**
 * Сервис для обработки и отправки уведомлений
 */
public interface NotificationService {

    /**
     * Обработка события уведомления
     */
    void processNotificationEvent(NotificationEvent event);

    /**
     * Отправка приветственного уведомления
     */
    void sendWelcomeNotification(Long userId, String username, String email);

    /**
     * Отправка уведомления об обновлении профиля
     */
    void sendProfileUpdatedNotification(Long userId, String username);

    /**
     * Отправка уведомления о создании квеста
     */
    void sendQuestCreatedNotification(Long questId, String title, Long authorId);

    /**
     * Отправка уведомления об обновлении квеста
     */
    void sendQuestUpdatedNotification(Long questId, String title, Long authorId);

    /**
     * Отправка уведомления о публикации квеста
     */
    void sendQuestPublishedNotification(Long questId, String title, Long authorId);

    /**
     * Отправка уведомления о начале игровой сессии
     */
    void sendGameSessionStartedNotification(Long userId, Long sessionId, Long questId);

    /**
     * Отправка уведомления о завершении игровой сессии
     */
    void sendGameSessionFinishedNotification(Long userId, Long sessionId, boolean completed);

    /**
     * Отправка уведомления о завершении уровня
     */
    void sendLevelCompletedNotification(Long userId, Long sessionId, Integer levelNumber);

    /**
     * Отправка уведомления о создании команды
     */
    void sendTeamCreatedNotification(Long teamId, String teamName, Long captainId);

    /**
     * Отправка уведомления об обновлении команды
     */
    void sendTeamUpdatedNotification(Long teamId, String teamName);

    /**
     * Отправка уведомления о добавлении участника в команду
     */
    void sendTeamMemberAddedNotification(Long teamId, String teamName, Long userId, String userName);

    /**
     * Отправка уведомления об удалении участника из команды
     */
    void sendTeamMemberRemovedNotification(Long teamId, String teamName, Long userId, String userName);

    /**
     * Отправка уведомления о загрузке файла
     */
    void sendFileUploadedNotification(Long userId, String fileName);

    /**
     * Отправка email уведомления
     */
    void sendEmailNotification(String to, String subject, String message);

    /**
     * Отправка Telegram уведомления
     */
    void sendTelegramNotification(Long userId, String message);
}