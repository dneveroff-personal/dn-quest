package dn.quest.notification.service;

import dn.quest.shared.events.notification.NotificationEvent;

import java.util.concurrent.CompletableFuture;
import java.util.UUID;

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
    CompletableFuture<Void> sendWelcomeNotification(UUID userId, String username, String email);

    /**
     * Отправка уведомления об обновлении профиля
     */
    CompletableFuture<Void> sendProfileUpdatedNotification(UUID userId, String username);

    /**
     * Отправка уведомления о создании квеста
     */
    CompletableFuture<Void> sendQuestCreatedNotification(UUID questId, String title, UUID authorId);

    /**
     * Отправка уведомления об обновлении квеста
     */
    CompletableFuture<Void> sendQuestUpdatedNotification(UUID questId, String title, UUID authorId);

    /**
     * Отправка уведомления о публикации квеста
     */
    CompletableFuture<Void> sendQuestPublishedNotification(UUID questId, String title, UUID authorId);

    /**
     * Отправка уведомления о начале игровой сессии
     */
    CompletableFuture<Void> sendGameSessionStartedNotification(UUID userId, UUID sessionId, UUID questId);

    /**
     * Отправка уведомления о завершении игровой сессии
     */
    CompletableFuture<Void> sendGameSessionFinishedNotification(UUID userId, UUID sessionId, boolean completed);

    /**
     * Отправка уведомления о завершении уровня
     */
    CompletableFuture<Void> sendLevelCompletedNotification(UUID userId, UUID sessionId, Integer levelNumber);

    /**
     * Отправка уведомления о создании команды
     */
    CompletableFuture<Void> sendTeamCreatedNotification(UUID teamId, String teamName, UUID captainId);

    /**
     * Отправка уведомления об обновлении команды
     */
    CompletableFuture<Void> sendTeamUpdatedNotification(UUID teamId, String teamName);

    /**
     * Отправка уведомления о добавлении участника в команду
     */
    CompletableFuture<Void> sendTeamMemberAddedNotification(UUID teamId, String teamName, UUID userId, String userName);

    /**
     * Отправка уведомления об удалении участника из команды
     */
    CompletableFuture<Void> sendTeamMemberRemovedNotification(UUID teamId, String teamName, UUID userId, String userName);

    /**
     * Отправка уведомления о загрузке файла
     */
    CompletableFuture<Void> sendFileUploadedNotification(UUID userId, String fileName);

    /**
     * Отправка email уведомления
     */
    CompletableFuture<Void> sendEmailNotification(String to, String subject, String message);

    /**
     * Отправка Telegram уведомления
     */
    CompletableFuture<Void> sendTelegramNotification(UUID userId, String message);
}