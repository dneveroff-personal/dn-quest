package dn.quest.gameengine.entity;

import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Квест - основная игровая сущность
 */
@Data
@Entity
@Table(name = "quests",
        indexes = {
                @Index(name = "idx_quest_number", columnList = "quest_number"),
                @Index(name = "idx_quest_type", columnList = "type"),
                @Index(name = "idx_quest_difficulty", columnList = "difficulty"),
                @Index(name = "idx_quest_published", columnList = "published"),
                @Index(name = "idx_quest_start_at", columnList = "start_at"),
                @Index(name = "idx_quest_end_at", columnList = "end_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"levels", "authors"})
@ToString(exclude = {"levels", "authors"})
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quest_number", unique = true)
    private Long number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private QuestType type = QuestType.TEAM;

    @Column(nullable = false, length = 300)
    private String title;

    @Lob
    private String descriptionHtml;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "min_team_size")
    private Integer minTeamSize;

    @Column(name = "max_team_size")
    private Integer maxTeamSize;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Level> levels = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "quest_authors",
            joinColumns = @JoinColumn(name = "quest_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> authors = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Проверяет, активен ли квест
     */
    public boolean isActive() {
        Instant now = Instant.now();
        boolean afterStart = startAt == null || !now.isBefore(startAt);
        boolean beforeEnd = endAt == null || !now.isAfter(endAt);
        return published && afterStart && beforeEnd;
    }

    /**
     * Проверяет, доступен ли квест для участия
     */
    public boolean isAvailable() {
        return isActive() && published;
    }

    /**
     * Проверяет, является ли это командным квестом
     */
    public boolean isTeamQuest() {
        return type == QuestType.TEAM;
    }

    /**
     * Проверяет, является ли это сольным квестом
     */
    public boolean isSoloQuest() {
        return type == QuestType.SOLO;
    }

    /**
     * Получает количество уровней в квесте
     */
    public int getLevelCount() {
        return levels != null ? levels.size() : 0;
    }

    /**
     * Получает сложность квеста в виде числа
     */
    public int getDifficultyLevel() {
        return difficulty != null ? difficulty.ordinal() : 0;
    }

    /**
     * Проверяет, есть ли ограничения по размеру команды
     */
    public boolean hasTeamSizeRestrictions() {
        return minTeamSize != null || maxTeamSize != null;
    }

    /**
     * Проверяет, допустим ли размер команды
     */
    public boolean isValidTeamSize(int teamSize) {
        if (!isTeamQuest()) {
            return false;
        }
        
        int min = minTeamSize != null ? minTeamSize : 1;
        int max = maxTeamSize != null ? maxTeamSize : Integer.MAX_VALUE;
        
        return teamSize >= min && teamSize <= max;
    }

    /**
     * Получает оставшееся время до окончания квеста
     */
    public long getRemainingTimeSeconds() {
        if (endAt == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(0, endAt.getEpochSecond() - Instant.now().getEpochSecond());
    }

    /**
     * Получает время до начала квеста
     */
    public long getTimeToStartSeconds() {
        if (startAt == null) {
            return 0;
        }
        return Math.max(0, startAt.getEpochSecond() - Instant.now().getEpochSecond());
    }

    /**
     * Получает список ID уровней
     */
    public java.util.List<Long> getLevelIds() {
        if (levels == null) {
            return java.util.Collections.emptyList();
        }
        return levels.stream()
            .map(Level::getId)
            .sorted()
            .collect(java.util.stream.Collectors.toList());
    }
}