package dn.quest.shared.events.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Событие уведомления
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private String eventId;
    private UUID userId;
    private String title;
    private String message;
    private String type;
}
