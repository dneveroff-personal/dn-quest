package dn.quest.gameengine.entity;

import dn.quest.shared.enums.AttemptResult;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;
import java.util.UUID;

/**
 * Попытка ввода кода в игровой сессии
 */
@Data
@Entity
@Table(name = "game_code_attempts",
        indexes = {
                @Index(name = "idx_attempt_session_level_time", columnList = "session_id,level_id,created_at"),
                @Index(name = "idx_attempt_session", columnList = "session_id"),
                @Index(name = "idx_attempt_level", columnList = "level_id"),
                @Index(name = "idx_attempt_user", columnList = "user_id"),
                @Index(name = "idx_attempt_result", columnList = "result"),
                @Index(name = "idx_attempt_created_at", columnList = "created_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"session", "level", "user", "matchedCode"})
@ToString(exclude = {"session", "level", "user", "matchedCode"})
public class CodeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "submitted_raw", nullable = false, length = 200)
    private String submittedRaw;

    @Column(name = "submitted_normalized", nullable = false, length = 200)
    private String submittedNormalized;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_code_id")
    private Code matchedCode;

    @Column(name = "matched_sector_no")
    private Integer matchedSectorNo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Проверяет, является ли попытка успешной
     */
    public boolean isSuccessful() {
        return result == AttemptResult.ACCEPTED_NORMAL ||
               result == AttemptResult.ACCEPTED_BONUS ||
               result == AttemptResult.ACCEPTED_PENALTY;
    }

    /**
     * Проверяет, является ли попытка дубликатом
     */
    public boolean isDuplicate() {
        return Boolean.TRUE.equals(isDuplicate) || result == AttemptResult.DUPLICATE;
    }

    /**
     * Проверяет, является ли попытка неправильной
     */
    public boolean isWrong() {
        return result == AttemptResult.INCORRECT;
    }

    /**
     * Получает время обработки попытки в миллисекундах
     */
    public Long getProcessingTimeMs() {
        return processingTimeMs != null ? processingTimeMs : 0L;
    }

    /**
     * Устанавливает время обработки попытки
     */
    public void setProcessingTime(Long startTimeMs) {
        if (startTimeMs != null) {
            this.processingTimeMs = System.currentTimeMillis() - startTimeMs;
        }
    }
}