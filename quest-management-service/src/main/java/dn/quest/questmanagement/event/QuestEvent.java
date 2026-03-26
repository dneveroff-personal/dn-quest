package dn.quest.questmanagement.event;

import dn.quest.shared.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Базовый класс для событий квестов
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestEvent extends BaseDTO {

    private String eventType;
    private Long questId;
    private Long questNumber;
    private String title;
    private String description;
    private String difficulty;
    private String questType;
    private String category;
    private Set<String> tags;
    private Set<Long> authorIds;
    private String status;
    private LocalDateTime timestamp;
    private Long userId;
    private String reason;

    public QuestEvent(String eventType, Long questId, Long userId) {
        this.eventType = eventType;
        this.questId = questId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }
}