package dn.quest.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа на пакетную отправку уведомлений
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchNotificationResponse {

    /**
     * Количество успешно отправленных уведомлений
     */
    private Integer successCount;

    /**
     * Количество уведомлений с ошибками
     */
    private Integer failureCount;

    /**
     * Список ID успешно отправленных уведомлений
     */
    private List<String> successNotifications;

    /**
     * Список ошибок для неудачных уведомлений
     */
    private List<String> failureNotifications;

    /**
     * Общее количество обработанных уведомлений
     */
    public Integer getTotalCount() {
        return successCount + failureCount;
    }

    /**
     * Процент успешных отправок
     */
    public Double getSuccessRate() {
        if (getTotalCount() == 0) {
            return 0.0;
        }
        return (double) successCount / getTotalCount() * 100;
    }
}