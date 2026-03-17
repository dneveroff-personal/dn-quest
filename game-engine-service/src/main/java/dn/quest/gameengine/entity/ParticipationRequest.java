package dn.quest.gameengine.entity;

import dn.quest.shared.enums.ParticipationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;

/**
 * Запрос на участие в игровой сессии
 */
@Data
@Entity
@Table(name = "participation_requests",
        indexes = {
                @Index(name = "idx_request_session", columnList = "session_id"),
                @Index(name = "idx_request_user", columnList = "user_id"),
                @Index(name = "idx_request_team", columnList = "team_id"),
                @Index(name = "idx_request_status", columnList = "status"),
                @Index(name = "idx_request_created_at", columnList = "created_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"session", "user", "team"})
@ToString(exclude = {"session", "user", "team"})
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status = ParticipationStatus.PENDING;

    @Column(name = "request_message", length = 500)
    private String requestMessage;

    @Column(name = "response_message", length = 500)
    private String responseMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedByUser;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "invitation_code")
    private String invitationCode;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        if (status != ParticipationStatus.PENDING && processedAt == null) {
            processedAt = Instant.now();
        }
    }

    /**
     * Проверяет, находится ли запрос в ожидании
     */
    public boolean isPending() {
        return status == ParticipationStatus.PENDING;
    }

    /**
     * Проверяет, одобрен ли запрос
     */
    public boolean isApproved() {
        return status == ParticipationStatus.APPROVED;
    }

    /**
     * Проверяет, отклонен ли запрос
     */
    public boolean isRejected() {
        return status == ParticipationStatus.REJECTED;
    }

    /**
     * Проверяет, отменен ли запрос
     */
    public boolean isCancelled() {
        return status == ParticipationStatus.CANCELLED;
    }

    /**
     * Одобрить запрос
     */
    public void approve(String responseMessage, User processedByUser) {
        this.status = ParticipationStatus.APPROVED;
        this.responseMessage = responseMessage;
        this.processedByUser = processedByUser;
    }

    /**
     * Отклонить запрос
     */
    public void reject(String responseMessage, User processedByUser) {
        this.status = ParticipationStatus.REJECTED;
        this.responseMessage = responseMessage;
        this.processedByUser = processedByUser;
    }

    /**
     * Отменить запрос
     */
    public void cancel() {
        this.status = ParticipationStatus.CANCELLED;
    }

    /**
     * Получает время ожидания ответа в секундах
     */
    public long getWaitingTimeSeconds() {
        Instant endTime = processedAt != null ? processedAt : Instant.now();
        return endTime.getEpochSecond() - createdAt.getEpochSecond();
    }

    /**
     * Проверяет, является ли запрос устаревшим (более 24 часов)
     */
    public boolean isExpired() {
        return getWaitingTimeSeconds() > 24 * 60 * 60;
    }

    /**
     * Проверяет, является ли это командным запросом
     */
    public boolean isTeamRequest() {
        return team != null;
    }

    /**
     * Проверяет, является ли это персональным запросом
     */
    public boolean isUserRequest() {
        return user != null;
    }

    /**
     * Получает тип запроса в виде строки
     */
    public String getRequestType() {
        if (isTeamRequest()) {
            return "TEAM";
        } else if (isUserRequest()) {
            return "USER";
        }
        return "UNKNOWN";
    }
}