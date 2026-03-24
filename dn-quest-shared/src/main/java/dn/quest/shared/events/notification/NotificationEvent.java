package dn.quest.shared.events.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие уведомления
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String eventId;
    private Long userId;
    private String title;
    private String message;
    private String type;
}
