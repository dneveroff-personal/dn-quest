package dn.quest.notification.service;

import dn.quest.shared.events.notification.NotificationEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для обработки и отправки уведомлений
 */
public interface NotificationService {

    /**
     * Обработка события уведомления
     */
    CompletableFuture<Void> processNotificationEvent(NotificationEvent event);

    /**
     * Отправка приветственного уведомления
     */
    CompletableFuture<Void> sendWelcomeNotification(Long userId, String username, String email);

    /**
     * Отправка уведомления об обновлении профиля
     */
    CompletableFuture<Void> sendProfileUpdatedNotification(Long userId, String username);

    /**
     * Отправка уведомления о создании квеста
     */
    CompletableFuture<Void> sendQuestCreatedNotification(Long questId, String title, Long authorId);

    /**
     * Отправка уведомления об обновлении квеста
     */
    CompletableFuture<Void> sendQuestUpdatedNotification(Long questId, String title, Long authorId);

    /**
     * Отправка уведомления о публикации квеста
     */
    CompletableFuture<Void> sendQuestPublishedNotification(Long questId, String title, Long authorId);

    /**
     * Отправка уведомления о начале игровой сессии
     */
    CompletableFuture<Void> sendGameSessionStartedNotification(Long userId, Long sessionId, Long questId);

    /**
     * Отправка уведомления о завершении игровой сессии
     */
    CompletableFuture<Void> sendGameSessionFinishedNotification(Long userId, Long sessionId, boolean completed);

    /**
     * Отправка уведомления о завершении уровня
     */
    CompletableFuture<Void> sendLevelCompletedNotification(Long userId, Long sessionId, Integer levelNumber);

    /**
     * Отправка уведомления о создании команды
     */
    CompletableFuture<Void> sendTeamCreatedNotification(Long teamId, String teamName, Long captainId);

    /**
     * Отправка уведомления об обновлении команды
     */
    CompletableFuture<Void> sendTeamUpdatedNotification(Long teamId, String teamName);

    /**
     * Отправка уведомления о добавлении участника в команду
     */
    CompletableFuture<Void> sendTeamMemberAddedNotification(Long teamId, String teamName, Long userId, String userName);

    /**
     * Отправка уведомления об удалении участника из команды
     */
    CompletableFuture<Void> sendTeamMemberRemovedNotification(Long teamId, String teamName, Long userId, String userName);

    /**
     * Отправка уведомления о загрузке файла
     */
    CompletableFuture<Void> sendFileUploadedNotification(Long userId, String fileName);

    /**
     * Отправка email уведомления
     */
    CompletableFuture<Void> sendEmailNotification(String to, String subject, String message);

    /**
     * Отправка Telegram уведомления
     */
    CompletableFuture<Void> sendTelegramNotification(Long userId, String message);
}