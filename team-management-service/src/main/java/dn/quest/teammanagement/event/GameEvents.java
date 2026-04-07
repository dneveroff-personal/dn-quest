package dn.quest.teammanagement.event;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * События связанные с игровыми сессиями
 */
public class GameEvents {

    /**
     * Событие начала игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionStartedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private Long startedBy;
        private String startedByUsername;
        private String startTime;
        private String difficulty;
        private Integer maxParticipants;
        private Integer currentParticipants;
    }

    /**
     * Событие завершения игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionFinishedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private String finishTime;
        private String duration;
        private String status; // COMPLETED, FAILED, ABORTED
        private Integer score;
        private Integer maxScore;
        private Double completionPercentage;
        private Integer levelsCompleted;
        private Integer totalLevels;
        private String finishReason;
    }

    /**
     * Событие присоединения участника к игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionParticipantJoinedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private Long participantId;
        private String participantUsername;
        private String joinTime;
        private String joinMethod; // AUTO, MANUAL, INVITATION
    }

    /**
     * Событие выхода участника из игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionParticipantLeftEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private Long participantId;
        private String participantUsername;
        private String leaveTime;
        private String leaveReason; // VOLUNTARY, KICKED, DISCONNECTED
        private String sessionDuration;
        private Integer score;
        private Integer levelsCompleted;
    }

    /**
     * Событие завершения уровня в игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionLevelCompletedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private UUID levelId;
        private String levelName;
        private Long participantId;
        private String participantUsername;
        private String completionTime;
        private Integer attempts;
        private Integer hintsUsed;
        private Integer score;
        private Integer maxScore;
        private String completionTimeSeconds;
        private Boolean isFirstCompletion;
    }

    /**
     * Событие использования подсказки в игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionHintUsedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private UUID levelId;
        private String levelName;
        private UUID hintId;
        private String hintText;
        private Long participantId;
        private String participantUsername;
        private String hintTime;
        private Integer hintNumber;
        private Integer hintsUsed;
        private Integer maxHints;
    }

    /**
     * Событие попытки ответа в игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionAttemptSubmittedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private UUID levelId;
        private String levelName;
        private Long attemptId;
        private String answer;
        private Long participantId;
        private String participantUsername;
        private String submitTime;
        private Boolean isCorrect;
        private String result; // CORRECT, INCORRECT, PARTIAL
        private Integer score;
        private Integer attemptNumber;
        private String feedback;
    }

    /**
     * Событие паузы в игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionPausedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private Long pausedBy;
        private String pausedByUsername;
        private String pauseTime;
        private String pauseReason;
        private String sessionDuration;
    }

    /**
     * Событие возобновления игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionResumedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private Long resumedBy;
        private String resumedByUsername;
        private String resumeTime;
        private String pauseDuration;
    }

    /**
     * Событие изменения статуса игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionStatusChangedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private String previousStatus;
        private String newStatus;
        private Long changedBy;
        private String changedByUsername;
        private String changeReason;
        private String changeTime;
    }

    /**
     * Событие статистики игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionStatisticsUpdatedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private String sessionDuration;
        private Integer totalAttempts;
        private Integer correctAttempts;
        private Integer incorrectAttempts;
        private Double accuracyRate;
        private Integer totalHintsUsed;
        private Integer levelsCompleted;
        private Integer totalLevels;
        private Integer totalScore;
        private Integer maxScore;
        private Double completionPercentage;
        private String lastActivityAt;
    }

    /**
     * Событие достижения в игровой сессии
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class GameSessionAchievementUnlockedEvent extends BaseEvent {
        private UUID sessionId;
        private UUID questId;
        private String questName;
        private UUID teamId;
        private String teamName;
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private Long participantId;
        private String participantUsername;
        private String unlockedAt;
        private String achievementIcon;
        private Integer points;
    }
}