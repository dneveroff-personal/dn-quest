package dn.quest.notification.controller;

import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.service.NotificationAnalyticsService;
import dn.quest.notification.service.channel.NotificationChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Контроллер для аналитики и мониторинга уведомлений
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics API", description = "API для аналитики и мониторинга уведомлений")
public class AnalyticsController {

    private final NotificationAnalyticsService analyticsService;

    public AnalyticsController(NotificationAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/statistics/overall")
    @Operation(summary = "Получить общую статистику уведомлений")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationAnalyticsService.NotificationStatistics> getOverallStatistics() {
        return ResponseEntity.ok(analyticsService.getOverallStatistics());
    }

    @GetMapping("/statistics/period")
    @Operation(summary = "Получить статистику за период")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationAnalyticsService.NotificationStatistics> getStatisticsForPeriod(
            @Parameter(description = "Начальная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Конечная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getStatisticsForPeriod(startDate, endDate));
    }

    @GetMapping("/statistics/by-type")
    @Operation(summary = "Получить статистику по типам уведомлений")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatisticsByType() {
        return ResponseEntity.ok(analyticsService.getStatisticsByType());
    }

    @GetMapping("/statistics/by-category")
    @Operation(summary = "Получить статистику по категориям уведомлений")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatisticsByCategory() {
        return ResponseEntity.ok(analyticsService.getStatisticsByCategory());
    }

    @GetMapping("/statistics/by-channel")
    @Operation(summary = "Получить статистику по каналам доставки")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatisticsByChannel() {
        return ResponseEntity.ok(analyticsService.getStatisticsByChannel());
    }

    @GetMapping("/statistics/by-status")
    @Operation(summary = "Получить статистику по статусам")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatisticsByStatus() {
        return ResponseEntity.ok(analyticsService.getStatisticsByStatus());
    }

    @GetMapping("/statistics/delivery-time")
    @Operation(summary = "Получить статистику времени доставки")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDeliveryTimeStatistics(
            @Parameter(description = "Начальная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Конечная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getDeliveryTimeStatistics(startDate, endDate));
    }

    @GetMapping("/statistics/top-users")
    @Operation(summary = "Получить топ пользователей по количеству уведомлений")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTopUsersByNotificationCount(
            @Parameter(description = "Лимит") @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopUsersByNotificationCount(limit));
    }

    @GetMapping("/statistics/errors")
    @Operation(summary = "Получить статистику ошибок")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationAnalyticsService.ErrorStatistics> getErrorStatistics() {
        return ResponseEntity.ok(analyticsService.getErrorStatistics());
    }

    @GetMapping("/performance")
    @Operation(summary = "Получить метрики производительности")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationAnalyticsService.PerformanceMetrics> getPerformanceMetrics() {
        return ResponseEntity.ok(analyticsService.getPerformanceMetrics());
    }

    @GetMapping("/trends")
    @Operation(summary = "Получить тренды уведомлений")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getNotificationTrends(
            @Parameter(description = "Начальная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Конечная дата") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(analyticsService.getNotificationTrends(startDate, endDate));
    }

    @GetMapping("/statistics/templates")
    @Operation(summary = "Получить статистику по шаблонам")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTemplateStatistics() {
        return ResponseEntity.ok(analyticsService.getTemplateStatistics());
    }

    @PostMapping("/metrics/notification-sent")
    @Operation(summary = "Записать метрику отправки уведомления")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> recordNotificationSent(
            @Parameter(description = "ID уведомления") @RequestParam String notificationId,
            @Parameter(description = "Тип уведомления") @RequestParam String type,
            @Parameter(description = "Канал доставки") @RequestParam String channel,
            @Parameter(description = "Время доставки в мс") @RequestParam long deliveryTimeMs) {
        // Создать простой канал на основе строки
        String channelType = channel != null ? channel.toUpperCase() : "UNKNOWN";
        analyticsService.recordNotificationSent(notificationId, type, channelType, deliveryTimeMs);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/notification-error")
    @Operation(summary = "Записать метрику ошибки")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> recordNotificationError(
            @Parameter(description = "ID уведомления") @RequestParam String notificationId,
            @Parameter(description = "Тип ошибки") @RequestParam String errorType,
            @Parameter(description = "Сообщение об ошибке") @RequestParam String errorMessage) {
        analyticsService.recordNotificationError(notificationId, errorType, errorMessage);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/notification-opened")
    @Operation(summary = "Записать метрику открытия уведомления")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> recordNotificationOpened(
            @Parameter(description = "ID уведомления") @RequestParam String notificationId,
            @Parameter(description = "Время открытия") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime openedAt) {
        analyticsService.recordNotificationOpened(notificationId, openedAt);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/metrics/notification-clicked")
    @Operation(summary = "Записать метрику клика по уведомлению")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> recordNotificationClicked(
            @Parameter(description = "ID уведомления") @RequestParam String notificationId,
            @Parameter(description = "Цель клика") @RequestParam String clickTarget,
            @Parameter(description = "Время клика") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime clickedAt) {
        analyticsService.recordNotificationClicked(notificationId, clickTarget, clickedAt);
        return ResponseEntity.ok().build();
    }
}