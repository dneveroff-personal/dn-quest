package dn.quest.teammanagement.entity;

import dn.quest.teammanagement.enums.TeamRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teams", 
       uniqueConstraints = @UniqueConstraint(name = "uk_team_name", columnNames = "name"),
       indexes = {
           @Index(name = "idx_team_captain", columnList = "captain_id"),
           @Index(name = "idx_team_created_at", columnList = "created_at")
       })
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id", nullable = false)
    private User captain;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 10;

    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TeamMember> members = new HashSet<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TeamInvitation> invitations = new HashSet<>();

    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeamSettings settings;

    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeamStatistics statistics;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Проверяет, является ли пользователь капитаном команды
     */
    public boolean isCaptain(Long userId) {
        return captain != null && captain.getId().equals(userId);
    }

    /**
     * Проверяет, является ли пользователь участником команды
     */
    public boolean isMember(Long userId) {
        return members.stream()
                .anyMatch(member -> member.getUser().getId().equals(userId));
    }

    /**
     * Получает роль пользователя в команде
     */
    public TeamRole getUserRole(Long userId) {
        return members.stream()
                .filter(member -> member.getUser().getId().equals(userId))
                .map(TeamMember::getRole)
                .findFirst()
                .orElse(null);
    }

    /**
     * Получает количество участников в команде
     */
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    /**
     * Проверяет, можно ли добавить нового участника
     */
    public boolean canAddMember() {
        return maxMembers == null || getMemberCount() < maxMembers;
    }
}