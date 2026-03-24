package dn.quest.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.entity.UserNotificationPreferences;
import dn.quest.notification.enums.*;
import dn.quest.notification.repository.NotificationRepository;
import dn.quest.notification.service.NotificationQueueService;
import dn.quest.notification.service.NotificationService;
import dn.quest.notification.service.NotificationTemplateService;
import dn.quest.notification.service.UserNotificationPreferencesService;
import dn.quest.notification.service.channel.NotificationChannelManager;
import dn.quest.notification.service.channel.NotificationChannelResult;
import dn.quest.shared.constants.ApplicationConstants;
import dn.quest.shared.events.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Улучшенная реализация сервиса уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnhancedNotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationChannelManager channelManager;
    private final NotificationTemplateService templateService;
    private final UserNotificationPreferencesService preferencesService;
    private final NotificationServiceImpl legacyNotificationService;
    private final NotificationQueueService queueService;

    @Value("${app.notification.default-retry-count:3}")
    private int defaultRetryCount;

    @Value("${app.notification.batch-size:100}")
    private int batchSize;

    @Value("${app.notification.use-queue:true}")
    private boolean useQueue;

    @Override
    @Async
    public CompletableFuture<Void> processNotificationEvent(NotificationEvent event) {
        log.info("Processing notification event: {} for user: {}", event.getType(), event.getUserId());

        try {
            // Создание уведомления из события
            Notification notification = createNotificationFromEvent(event);
            
            // Сохранение уведомления
            notification = notificationRepository.save(notification);
            
            // Отправка уведомления
            sendNotificationAsync(notification);
            
        } catch (Exception e) {
            log.error("Error processing notification event: {}", event.getEventId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendWelcomeNotification(Long userId, String username, String email) {
        log.info("Sending welcome notification to user: {}", userId);

        try {
            // Получение или создание предпочтений пользователя
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(preferencesService.createPreferences(userId));

            // Обновление email если нужно
            if (email != null && !email.equals(preferences.getEmail())) {
                preferencesService.updateEmail(userId, email);
            }

            // Создание уведомлений для разных каналов
            List<Notification> notifications = createWelcomeNotifications(userId, username, email, preferences);
            
            // Отправка уведомлений
            for (Notification notification : notifications) {
                sendNotificationAsync(notification);
            }

        } catch (Exception e) {
            log.error("Error sending welcome notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendProfileUpdatedNotification(Long userId, String username) {
        log.info("Sending profile updated notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getSystemEnabled()) {
                List<Notification> notifications = createProfileUpdatedNotifications(userId, username, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending profile updated notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendQuestCreatedNotification(Long questId, String title, Long authorId) {
        log.info("Sending quest created notification to author: {}", authorId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(authorId)
                    .orElse(null);

            if (preferences != null && preferences.getQuestEnabled()) {
                List<Notification> notifications = createQuestCreatedNotifications(questId, title, authorId, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending quest created notification to author: {}", authorId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendQuestUpdatedNotification(Long questId, String title, Long authorId) {
        log.info("Sending quest updated notification for quest: {}", questId);
        // Broadcast notification - notify all subscribers
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendQuestPublishedNotification(Long questId, String title, Long authorId) {
        log.info("Sending quest published notification to author: {}", authorId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(authorId)
                    .orElse(null);

            if (preferences != null && preferences.getQuestEnabled()) {
                List<Notification> notifications = createQuestPublishedNotifications(questId, title, authorId, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending quest published notification to author: {}", authorId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendGameSessionStartedNotification(Long userId, Long sessionId, Long questId) {
        log.info("Sending game session started notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getGameEnabled()) {
                List<Notification> notifications = createGameSessionStartedNotifications(userId, sessionId, questId, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending game session started notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendGameSessionFinishedNotification(Long userId, Long sessionId, boolean completed) {
        log.info("Sending game session finished notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getGameEnabled()) {
                List<Notification> notifications = createGameSessionFinishedNotifications(userId, sessionId, completed, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending game session finished notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendLevelCompletedNotification(Long userId, Long sessionId, Integer levelNumber) {
        log.info("Sending level completed notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getGameEnabled()) {
                List<Notification> notifications = createLevelCompletedNotifications(userId, sessionId, levelNumber, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending level completed notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTeamCreatedNotification(Long teamId, String teamName, Long captainId) {
        log.info("Sending team created notification to captain: {}", captainId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(captainId)
                    .orElse(null);

            if (preferences != null && preferences.getTeamEnabled()) {
                List<Notification> notifications = createTeamCreatedNotifications(teamId, teamName, captainId, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending team created notification to captain: {}", captainId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTeamUpdatedNotification(Long teamId, String teamName) {
        log.info("Sending team updated notification for team: {}", teamId);

        try {
            // Здесь можно добавить логику для отправки уведомлений всем участникам команды
            log.debug("Team updated notification for team {}: {}", teamId, teamName);
        } catch (Exception e) {
            log.error("Error sending team updated notification for team: {}", teamId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTeamMemberAddedNotification(Long teamId, String teamName, Long userId, String userName) {
        log.info("Sending team member added notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getTeamEnabled()) {
                List<Notification> notifications = createTeamMemberAddedNotifications(teamId, teamName, userId, userName, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending team member added notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTeamMemberRemovedNotification(Long teamId, String teamName, Long userId, String userName) {
        log.info("Sending team member removed notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getTeamEnabled()) {
                List<Notification> notifications = createTeamMemberRemovedNotifications(teamId, teamName, userId, userName, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending team member removed notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendFileUploadedNotification(Long userId, String fileName) {
        log.info("Sending file uploaded notification to user: {}", userId);

        try {
            UserNotificationPreferences preferences = preferencesService.getPreferences(userId)
                    .orElse(null);

            if (preferences != null && preferences.getSystemEnabled()) {
                List<Notification> notifications = createFileUploadedNotifications(userId, fileName, preferences);
                
                for (Notification notification : notifications) {
                    sendNotificationAsync(notification);
                }
            }

        } catch (Exception e) {
            log.error("Error sending file uploaded notification to user: {}", userId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendEmailNotification(String to, String subject, String message) {
        log.debug("Delegating email notification to legacy service");
        return legacyNotificationService.sendEmailNotification(to, subject, message);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTelegramNotification(Long userId, String message) {
        log.debug("Delegating telegram notification to legacy service");
        return legacyNotificationService.sendTelegramNotification(userId, message);
    }

    /**
     * Создать уведомление из события
     */
    private Notification createNotificationFromEvent(NotificationEvent event) {
        return Notification.builder()
                .notificationId(event.getEventId())
                .userId(event.getUserId())
                .type(NotificationType.EMAIL)
                .category(NotificationCategory.SYSTEM)
                .priority(NotificationPriority.NORMAL)
                .subject(event.getTitle())
                .content(event.getMessage())
                .htmlContent(null)
                .templateData(null)
                .relatedEntityId(null)
                .relatedEntityType(null)
                .sourceEventId(event.getEventId())
                .sourceEventType(event.getType())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(defaultRetryCount)
                .createdAt(Instant.now())
                .scheduledAt(null)
                .correlationId(event.getEventId())
                .metadata(null)
                .build();
    }

    /**
     * Асинхронная отправка уведомления
     */
    @Async
    protected CompletableFuture<Void> sendNotificationAsync(Notification notification) {
        try {
            // Проверка предпочтений пользователя
            if (!checkUserPreferences(notification)) {
                log.debug("Notification {} skipped due to user preferences", notification.getNotificationId());
                return CompletableFuture.completedFuture(null);
            }

            // Проверка Do Not Disturb
            if (preferencesService.isDoNotDisturbActive(notification.getUserId())) {
                log.debug("Notification {} skipped due to Do Not Disturb mode", notification.getNotificationId());
                return CompletableFuture.completedFuture(null);
            }

            // Проверка лимитов
            if (!preferencesService.checkNotificationLimits(notification.getUserId())) {
                log.debug("Notification {} skipped due to rate limits", notification.getNotificationId());
                return CompletableFuture.completedFuture(null);
            }

            // Обработка шаблонов если нужно
            processTemplates(notification);

            // Сохранение уведомления
            notification = notificationRepository.save(notification);

            if (useQueue) {
                // Добавление в очередь для асинхронной обработки
                addToQueue(notification);
            } else {
                // Прямая отправка (для обратной совместимости)
                sendDirectly(notification);
            }

        } catch (Exception e) {
            log.error("Error sending notification: {}", notification.getNotificationId(), e);
            updateNotificationError(notification, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Добавить уведомление в очередь
     */
    private void addToQueue(Notification notification) {
        try {
            // Определяем каналы для отправки
            List<String> channels = determineChannels(notification);
            
            for (String channel : channels) {
                String payload = new ObjectMapper().writeValueAsString(notification);
                
                queueService.addToQueue(
                    notification.getId(),
                    notification.getUserId(),
                    channel,
                    notification.getPriority(),
                    payload,
                    notification.getScheduledAt() != null ?
                        java.time.LocalDateTime.ofInstant(notification.getScheduledAt(), java.time.ZoneId.systemDefault()) : null
                );
            }
            
            log.debug("Notification {} added to queue for channels: {}",
                     notification.getNotificationId(), channels);
                     
        } catch (Exception e) {
            log.error("Error adding notification to queue: {}", notification.getNotificationId(), e);
            throw new RuntimeException("Failed to add notification to queue", e);
        }
    }

    /**
     * Отправить уведомление напрямую (без очереди)
     */
    private void sendDirectly(Notification notification) {
        try {
            // Отправка через канал
            NotificationChannelResult result = channelManager.sendNotification(notification);

            // Обновление статуса уведомления
            updateNotificationStatus(notification, result);
            
        } catch (Exception e) {
            log.error("Error sending notification directly: {}", notification.getNotificationId(), e);
            updateNotificationError(notification, e.getMessage());
        }
    }

    /**
     * Определить каналы для отправки уведомления
     */
    private List<String> determineChannels(Notification notification) {
        List<String> channels = new ArrayList<>();
        
        UserNotificationPreferences preferences = preferencesService.getPreferences(notification.getUserId())
            .orElse(null);
            
        if (preferences == null) {
            // Если предпочтений нет, используем in-app по умолчанию
            channels.add("IN_APP");
            return channels;
        }
        
        // Определяем каналы на основе типа уведомления и предпочтений
        switch (notification.getType()) {
            case EMAIL:
                if (preferences.getEmailEnabled() && notification.getRecipientEmail() != null) {
                    channels.add("EMAIL");
                }
                break;
            case PUSH:
                if (preferences.getPushEnabled() && notification.getFcmToken() != null) {
                    channels.add("PUSH");
                }
                break;
            case IN_APP:
                if (preferences.getInAppEnabled()) {
                    channels.add("IN_APP");
                }
                break;
            case TELEGRAM:
                if (preferences.getTelegramEnabled() && notification.getTelegramChatId() != null) {
                    channels.add("TELEGRAM");
                }
                break;
            case SMS:
                if (preferences.getSmsEnabled() && notification.getRecipientPhone() != null) {
                    channels.add("SMS");
                }
                break;
        }
        
        // Всегда добавляем in-app для важных уведомлений
        if (notification.getPriority() == NotificationPriority.HIGH ||
            notification.getPriority() == NotificationPriority.URGENT) {
            if (!channels.contains("IN_APP")) {
                channels.add("IN_APP");
            }
        }
        
        return channels;
    }

    /**
     * Проверить пользовательские предпочтения
     */
    private boolean checkUserPreferences(Notification notification) {
        // Проверка типа уведомлений
        if (!preferencesService.isNotificationTypeEnabled(notification.getUserId(), notification.getType())) {
            return false;
        }

        // Проверка категории уведомлений
        if (!preferencesService.isNotificationCategoryEnabled(notification.getUserId(), notification.getCategory().getValue())) {
            return false;
        }

        return true;
    }

    /**
     * Обработать шаблоны уведомления
     */
    private void processTemplates(Notification notification) {
        try {
            // Если есть templateData, обрабатываем шаблоны
            if (notification.getTemplateData() != null && !notification.getTemplateData().isEmpty()) {
                Map<String, Object> variables = parseTemplateData(notification.getTemplateData());
                
                // Обработка текстового содержимого
                if (notification.getContent() != null) {
                    String processedContent = templateService.processTemplate(
                            "default_" + notification.getCategory().getValue(), variables);
                    if (processedContent != null) {
                        notification.setContent(processedContent);
                    }
                }

                // Обработка HTML содержимого
                if (notification.getHtmlContent() != null) {
                    String processedHtml = templateService.processHtmlTemplate(
                            "default_" + notification.getCategory().getValue() + "_html", variables);
                    if (processedHtml != null) {
                        notification.setHtmlContent(processedHtml);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to process templates for notification: {}", notification.getNotificationId(), e);
        }
    }

    /**
     * Парсинг данных шаблона
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseTemplateData(String templateData) {
        try {
            return new ObjectMapper().readValue(templateData, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse template data: {}", templateData, e);
            return new HashMap<>();
        }
    }

    /**
     * Обновить статус уведомления
     */
    private void updateNotificationStatus(Notification notification, NotificationChannelResult result) {
        if (result.isSuccess()) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(result.getSentAt());
            if (result.getDeliveredAt() != null) {
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveredAt(result.getDeliveredAt());
            }
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(result.getErrorMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        }

        notificationRepository.save(notification);
    }

    /**
     * Обновить ошибку уведомления
     */
    private void updateNotificationError(Notification notification, String errorMessage) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(errorMessage);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);
    }

    // Методы создания уведомлений для разных сценариев

    private List<Notification> createWelcomeNotifications(Long userId, String username, String email, 
                                                         UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "welcome_" + userId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "username", username,
                "userId", userId,
                "email", email
        );

        if (preferences.getEmailEnabled() && email != null) {
            notifications.add(createNotification(
                    notificationId + "_email",
                    userId,
                    NotificationType.EMAIL,
                    NotificationCategory.WELCOME,
                    "Добро пожаловать в DN Quest!",
                    generateWelcomeContent(username),
                    null,
                    email,
                    null,
                    null,
                    null,
                    null,
                    null,
                    variables
            ));
        }

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.WELCOME,
                    "Добро пожаловать!",
                    "Добро пожаловать в DN Quest, " + username + "!",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createProfileUpdatedNotifications(Long userId, String username, 
                                                               UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "profile_updated_" + userId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "username", username,
                "userId", userId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.SYSTEM,
                    "Профиль обновлен",
                    "Ваш профиль был успешно обновлен.",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createQuestCreatedNotifications(Long questId, String title, Long authorId, 
                                                              UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "quest_created_" + questId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "questId", questId,
                "title", title,
                "authorId", authorId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    authorId,
                    NotificationType.IN_APP,
                    NotificationCategory.QUEST,
                    "Квест создан",
                    "Ваш квест \"" + title + "\" был успешно создан.",
                    null,
                    null,
                    null,
                    null,
                    questId.toString(),
                    "quest",
                    ApplicationConstants.Quest.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createQuestUpdatedNotifications(Long questId, String title, Long authorId, 
                                                              UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "quest_updated_" + questId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "questId", questId,
                "title", title,
                "authorId", authorId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    authorId,
                    NotificationType.IN_APP,
                    NotificationCategory.QUEST,
                    "Квест обновлен",
                    "Ваш квест \"" + title + "\" был обновлен.",
                    null,
                    null,
                    null,
                    null,
                    questId.toString(),
                    "quest",
                    ApplicationConstants.Quest.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createQuestPublishedNotifications(Long questId, String title, Long authorId, 
                                                                UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "quest_published_" + questId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "questId", questId,
                "title", title,
                "authorId", authorId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    authorId,
                    NotificationType.IN_APP,
                    NotificationCategory.QUEST,
                    "Квест опубликован",
                    "Поздравляем! Ваш квест \"" + title + "\" опубликован.",
                    null,
                    null,
                    null,
                    null,
                    questId.toString(),
                    "quest",
                    ApplicationConstants.Quest.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createGameSessionStartedNotifications(Long userId, Long sessionId, Long questId, 
                                                                    UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "game_started_" + sessionId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "sessionId", sessionId,
                "questId", questId,
                "userId", userId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.GAME,
                    "Игра начата",
                    "Вы начали игровую сессию. Удачи!",
                    null,
                    null,
                    null,
                    null,
                    sessionId.toString(),
                    "game",
                    ApplicationConstants.Game.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createGameSessionFinishedNotifications(Long userId, Long sessionId, boolean completed, 
                                                                     UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "game_finished_" + sessionId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "sessionId", sessionId,
                "completed", completed,
                "userId", userId
        );

        String subject = completed ? "Квест завершен!" : "Игра завершена";
        String content = completed ? "Поздравляем с завершением квеста!" : "Ваша игровая сессия завершена.";

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.GAME,
                    subject,
                    content,
                    null,
                    null,
                    null,
                    null,
                    sessionId.toString(),
                    "game",
                    ApplicationConstants.Game.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createLevelCompletedNotifications(Long userId, Long sessionId, Integer levelNumber, 
                                                               UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "level_completed_" + sessionId + "_" + levelNumber + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "sessionId", sessionId,
                "levelNumber", levelNumber,
                "userId", userId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.GAME,
                    "Уровень пройден!",
                    "Отлично! Вы прошли уровень " + levelNumber,
                    null,
                    null,
                    null,
                    null,
                    sessionId.toString(),
                    "game",
                    ApplicationConstants.Game.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createTeamCreatedNotifications(Long teamId, String teamName, Long captainId, 
                                                             UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "team_created_" + teamId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "teamId", teamId,
                "teamName", teamName,
                "captainId", captainId
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    captainId,
                    NotificationType.IN_APP,
                    NotificationCategory.TEAM,
                    "Команда создана",
                    "Ваша команда \"" + teamName + "\" создана. Вы капитан.",
                    null,
                    null,
                    null,
                    null,
                    teamId.toString(),
                    "team",
                    ApplicationConstants.Team.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createTeamMemberAddedNotifications(Long teamId, String teamName, Long userId, String userName, 
                                                                 UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "team_member_added_" + teamId + "_" + userId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "teamId", teamId,
                "teamName", teamName,
                "userId", userId,
                "userName", userName
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.TEAM,
                    "Добро пожаловать в команду!",
                    "Вы добавлены в команду \"" + teamName + "\"",
                    null,
                    null,
                    null,
                    null,
                    teamId.toString(),
                    "team",
                    ApplicationConstants.Team.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createTeamMemberRemovedNotifications(Long teamId, String teamName, Long userId, String userName, 
                                                                  UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "team_member_removed_" + teamId + "_" + userId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "teamId", teamId,
                "teamName", teamName,
                "userId", userId,
                "userName", userName
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.TEAM,
                    "Вы покинули команду",
                    "Вы удалены из команды \"" + teamName + "\"",
                    null,
                    null,
                    null,
                    null,
                    teamId.toString(),
                    "team",
                    ApplicationConstants.Team.class.getTypeName(),
                    variables
            ));
        }

        return notifications;
    }

    private List<Notification> createFileUploadedNotifications(Long userId, String fileName, 
                                                             UserNotificationPreferences preferences) {
        List<Notification> notifications = new ArrayList<>();
        String notificationId = "file_uploaded_" + userId + "_" + System.currentTimeMillis();

        Map<String, Object> variables = Map.of(
                "userId", userId,
                "fileName", fileName
        );

        if (preferences.getInAppEnabled()) {
            notifications.add(createNotification(
                    notificationId + "_inapp",
                    userId,
                    NotificationType.IN_APP,
                    NotificationCategory.SYSTEM,
                    "Файл загружен",
                    "Ваш файл \"" + fileName + "\" успешно загружен",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    variables
            ));
        }

        return notifications;
    }

    /**
     * Создать уведомление
     */
    private Notification createNotification(String notificationId, Long userId, NotificationType type, 
                                          NotificationCategory category, String subject, String content, 
                                          String htmlContent, String email, String phone, 
                                          String telegramChatId, String fcmToken, 
                                          String relatedEntityId, String relatedEntityType, 
                                          Map<String, Object> templateData) {
        try {
            return Notification.builder()
                    .notificationId(notificationId)
                    .userId(userId)
                    .recipientEmail(email)
                    .recipientPhone(phone)
                    .telegramChatId(telegramChatId)
                    .fcmToken(fcmToken)
                    .type(type)
                    .category(category)
                    .priority(NotificationPriority.NORMAL)
                    .subject(subject)
                    .content(content)
                    .htmlContent(htmlContent)
                    .templateData(templateData != null ? new ObjectMapper().writeValueAsString(templateData) : null)
                    .relatedEntityId(relatedEntityId)
                    .relatedEntityType(relatedEntityType)
                    .status(NotificationStatus.PENDING)
                    .retryCount(0)
                    .maxRetries(defaultRetryCount)
                    .createdAt(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("Error creating notification: {}", notificationId, e);
            throw new RuntimeException("Failed to create notification", e);
        }
    }

    /**
     * Сгенерировать приветственный контент
     */
    private String generateWelcomeContent(String username) {
        return String.format(
                "Здравствуйте, %s!\n\n" +
                "Добро пожаловать в DN Quest - увлекательный мир программирования и квестов!\n\n" +
                "Мы рады видеть вас в нашем сообществе. Начните свое путешествие:\n" +
                "• Изучайте доступные квесты\n" +
                "• Присоединяйтесь к командам\n" +
                "• Развивайте свои навыки программирования\n\n" +
                "Удачи в ваших приключениях!\n" +
                "Команда DN Quest",
                username
        );
    }
}