package dn.quest.notification.service.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Результат отправки уведомления через канал
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChannelResult {

    /**
     * Успешность отправки
     */
    private boolean success;

    /**
     * ID сообщения во внешней системе
     */
    private String externalMessageId;

    /**
     * Время отправки
     */
    private Instant sentAt;

    /**
     * Время доставки
     */
    private Instant deliveredAt;

    /**
     * Ошибка (если неуспешно)
     */
    private String errorMessage;

    /**
     * Код ошибки
     */
    private String errorCode;

    /**
     * Дополнительные метаданные
     */
    private Map<String, Object> metadata;

    /**
     * Стоимость отправки
     */
    private Double cost;

    /**
     * Создать успешный результат
     */
    public static NotificationChannelResult success(String externalMessageId) {
        return NotificationChannelResult.builder()
                .success(true)
                .externalMessageId(externalMessageId)
                .sentAt(Instant.now())
                .build();
    }

    /**
     * Создать успешный результат с метаданными
     */
    public static NotificationChannelResult success(String externalMessageId, Map<String, Object> metadata) {
        return NotificationChannelResult.builder()
                .success(true)
                .externalMessageId(externalMessageId)
                .sentAt(Instant.now())
                .metadata(metadata)
                .build();
    }

    /**
     * Создать неуспешный результат
     */
    public static NotificationChannelResult failure(String errorMessage, String errorCode) {
        return NotificationChannelResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .sentAt(Instant.now())
                .build();
    }

    /**
     * Создать неуспешный результат с кодом ошибки
     */
    public static NotificationChannelResult failure(String errorMessage) {
        return failure(errorMessage, null);
    }
}