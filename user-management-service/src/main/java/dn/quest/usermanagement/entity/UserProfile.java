package dn.quest.usermanagement.entity;

import dn.quest.shared.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность профиля пользователя для User Management Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_profiles_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_user_profiles_user_id", columnList = "user_id"),
                @Index(name = "idx_user_profiles_public_name", columnList = "public_name"),
                @Index(name = "idx_user_profiles_is_active", columnList = "is_active"),
                @Index(name = "idx_user_profiles_created_at", columnList = "created_at")
        })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "public_name", length = 128)
    private String publicName;

    @Column(name = "role", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.PLAYER;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "location", length = 128)
    private String location;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "blocked_until")
    private Instant blockedUntil;

    @Column(name = "block_reason", length = 500)
    private String blockReason;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Проверяет, заблокирован ли пользователь
     */
    public boolean isCurrentlyBlocked() {
        return isBlocked && (blockedUntil == null || blockedUntil.isAfter(Instant.now()));
    }

    /**
     * Разблокирует пользователя
     */
    public void unblock() {
        this.isBlocked = false;
        this.blockedUntil = null;
        this.blockReason = null;
    }

    /**
     * Блокирует пользователя
     */
    public void block(String reason, Instant blockedUntil) {
        this.isBlocked = true;
        this.blockReason = reason;
        this.blockedUntil = blockedUntil;
    }

    /**
     * Обновляет время последней активности
     */
    public void updateLastActivity() {
        this.lastActivityAt = Instant.now();
    }
}