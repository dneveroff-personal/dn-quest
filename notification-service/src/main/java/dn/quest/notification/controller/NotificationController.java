package dn.quest.notification.controller;

import dn.quest.notification.dto.*;
import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.service.NotificationService;
import dn.quest.notification.service.channel.NotificationChannelManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST контроллер для управления уведомлениями
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "API для управления уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationChannelManager channelManager;

    @Operation(summary = "Отправить уведомление", description = "Отправить уведомление указанному пользователю")
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public CompletableFuture<ResponseEntity<ApiResponse<NotificationDTO>>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        
        log.info("Sending notification to user: {}", request.getUserId());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Создание уведомления из запроса
                Notification notification = createNotificationFromRequest(request);
                
                // Отправка уведомления
                notificationService.processNotificationEvent(createNotificationEvent(notification));
                
                NotificationDTO response = convertToDTO(notification);
                
                return ResponseEntity.ok(ApiResponse.success(response, "Notification sent successfully"));
                
            } catch (Exception e) {
                log.error("Error sending notification", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to send notification: " + e.getMessage()));
            }
        });
    }

    @Operation(summary = "Получить уведомления пользователя", description = "Получить список уведомлений для указанного пользователя")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationDTO>>> getUserNotifications(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Статус уведомления") @RequestParam(required = false) NotificationStatus status,
            @Parameter(description = "Тип уведомления") @RequestParam(required = false) NotificationType type) {
        
        log.info("Getting notifications for user: {}, page: {}, size: {}", userId, page, size);
        
        try {
            // Здесь должна быть логика получения уведомлений из сервиса
            // Временно возвращаем пустой результат
            PagedResponse<NotificationDTO> response = new PagedResponse<NotificationDTO>(
                    List.of(), page, size, 0L, 0, 0
            );
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error getting notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get notifications: " + e.getMessage()));
        }
    }

    @Operation(summary = "Отметить уведомление как прочитанное", description = "Пометить указанное уведомление как прочитанное")
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or @notificationService.isNotificationOwner(#id, authentication.principal.id)")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(description = "ID уведомления") @PathVariable String id) {
        
        log.info("Marking notification as read: {}", id);
        
        try {
            // Здесь должна быть логика отметки уведомления как прочитанного
            // Временно возвращаем успех
            
            return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
            
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to mark notification as read: " + e.getMessage()));
        }
    }

    @Operation(summary = "Пакетная отправка уведомлений", description = "Отправить уведомления нескольким пользователям")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<ApiResponse<BatchNotificationResponse>>> sendBatchNotifications(
            @Valid @RequestBody BatchNotificationRequest request) {
        
        log.info("Sending batch notifications to {} users", request.getUserIds().size());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> sentNotifications = new ArrayList<>();
                List<String> failedNotifications = new ArrayList<>();
                
                for (UUID userId : request.getUserIds()) {
                    try {
                        Notification notification = createNotificationFromBatchRequest(request, userId);
                        notificationService.processNotificationEvent(createNotificationEvent(notification));
                        sentNotifications.add(notification.getNotificationId());
                    } catch (Exception e) {
                        log.error("Failed to send notification to user: {}", userId, e);
                        failedNotifications.add("user:" + userId + " - " + e.getMessage());
                    }
                }
                
                BatchNotificationResponse response = new BatchNotificationResponse(
                        sentNotifications.size(),
                        failedNotifications.size(),
                        sentNotifications,
                        failedNotifications
                );
                
                return ResponseEntity.ok(ApiResponse.success(response, "Batch notifications processed"));
                
            } catch (Exception e) {
                log.error("Error sending batch notifications", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to send batch notifications: " + e.getMessage()));
            }
        });
    }

    @Operation(summary = "Получить статистику каналов", description = "Получить статистику по каналам доставки уведомлений")
    @GetMapping("/channels/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getChannelStatistics() {
        
        log.info("Getting channel statistics");
        
        try {
            Object statistics = channelManager.getChannelStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("Error getting channel statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get channel statistics: " + e.getMessage()));
        }
    }

    @Operation(summary = "Проверить здоровье каналов", description = "Проверить доступность каналов доставки")
    @GetMapping("/channels/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> checkChannelsHealth() {
        
        log.info("Checking channels health");
        
        try {
            Object health = channelManager.checkChannelsHealth();
            
            return ResponseEntity.ok(ApiResponse.success(health));
            
        } catch (Exception e) {
            log.error("Error checking channels health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check channels health: " + e.getMessage()));
        }
    }

    @Operation(summary = "Удалить старые уведомления", description = "Удалить уведомления старше указанной даты")
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldNotifications(
            @Parameter(description = "Количество дней для хранения") @RequestParam(defaultValue = "30") int days) {
        
        log.info("Cleaning up notifications older than {} days", days);
        
        try {
            // Здесь должна быть логика удаления старых уведомлений
            // Временно возвращаем 0
            int deletedCount = 0;
            
            return ResponseEntity.ok(ApiResponse.success(deletedCount, "Cleaned up " + deletedCount + " notifications"));
            
        } catch (Exception e) {
            log.error("Error cleaning up old notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cleanup notifications: " + e.getMessage()));
        }
    }

    /**
     * Создать уведомление из запроса
     */
    private Notification createNotificationFromRequest(SendNotificationRequest request) {
        // Временная реализация - должна быть заменена на реальную
        return Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .type(request.getType())
                .category(request.getCategory())
                .subject(request.getSubject())
                .content(request.getContent())
                .htmlContent(request.getHtmlContent())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .telegramChatId(request.getTelegramChatId())
                .fcmToken(request.getFcmToken())
                .build();
    }

    /**
     * Создать уведомление из пакетного запроса
     */
    private Notification createNotificationFromBatchRequest(BatchNotificationRequest request, UUID userId) {
        return Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .userId(userId)
                .type(request.getType())
                .category(request.getCategory())
                .subject(request.getSubject())
                .content(request.getContent())
                .htmlContent(request.getHtmlContent())
                .build();
    }

    /**
     * Создать событие уведомления
     */
    private dn.quest.shared.events.notification.NotificationEvent createNotificationEvent(Notification notification) {
        // Временная реализация - должна быть заменена на реальную
        return dn.quest.shared.events.notification.NotificationEvent.builder()
                .eventId(notification.getNotificationId())
                .userId(notification.getUserId())
                .title(notification.getSubject())
                .message(notification.getContent())
                .type(notification.getType().getValue())
                .build();
    }

    /**
     * Конвертировать уведомление в DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .category(notification.getCategory())
                .priority(notification.getPriority())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .htmlContent(notification.getHtmlContent())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .build();
    }
}