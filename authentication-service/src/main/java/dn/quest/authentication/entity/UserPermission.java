package dn.quest.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Сущность для связи пользователя с разрешениями
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_permissions_user_permission", 
                                 columnNames = {"user_id", "permission_id"})
        },
        indexes = {
                @Index(name = "idx_user_permissions_user_id", columnList = "user_id"),
                @Index(name = "idx_user_permissions_permission_id", columnList = "permission_id")
        })
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    @Column(name = "granted_by")
    private String grantedBy;

    @PrePersist
    protected void onCreate() {
        grantedAt = Instant.now();
    }
}