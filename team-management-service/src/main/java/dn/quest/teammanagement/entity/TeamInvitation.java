package dn.quest.teammanagement.entity;

import dn.quest.shared.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность приглашения в команду
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_invitations",
       uniqueConstraints = @UniqueConstraint(name = "uk_team_user_invite", columnNames = {"team_id", "user_id"}),
       indexes = {
           @Index(name = "idx_invitation_user", columnList = "user_id"),
           @Index(name = "idx_invitation_status", columnList = "status"),
           @Index(name = "idx_invitation_created_at", columnList = "created_at"),
           @Index(name = "idx_invitation_expires_at", columnList = "expires_at")
       })
public class TeamInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "invitation_message", length = 500)
    private String invitationMessage;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "response_message", length = 500)
    private String responseMessage;

    @PrePersist
    public void prePersist() {
        // Устанавливаем срок действия приглашения (7 дней по умолчанию)
        if (expiresAt == null) {
            this.expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 дней
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        if (status != InvitationStatus.PENDING && respondedAt == null) {
            this.respondedAt = Instant.now();
        }
    }

    /**
     * Проверяет, активно ли приглашение
     */
    public boolean isActive() {
        return InvitationStatus.PENDING.equals(status) && 
               (expiresAt == null || expiresAt.isAfter(Instant.now()));
    }

    /**
     * Проверяет, истекло ли приглашение
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    /**
     * Принимает приглашение
     */
    public void accept(String responseMessage) {
        this.status = InvitationStatus.ACCEPTED;
        this.responseMessage = responseMessage;
        this.respondedAt = Instant.now();
    }

    /**
     * Отклоняет приглашение
     */
    public void decline(String responseMessage) {
        this.status = InvitationStatus.CANCELLED;
        this.responseMessage = responseMessage;
        this.respondedAt = Instant.now();
    }

    /**
     * Помечает приглашение как истекшее
     */
    public void expire() {
        this.status = InvitationStatus.EXPIRED;
        this.respondedAt = Instant.now();
    }

    /**
     * Проверяет, может ли пользователь ответить на приглашение
     */
    public boolean canRespond(UUID userId) {
        return user != null && user.getId().equals(userId) && isActive();
    }
}