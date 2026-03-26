package dn.quest.questmanagement.entity;

import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import dn.quest.shared.dto.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность квеста в системе управления квестами
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quests",
       indexes = {
           @Index(name = "idx_quest_number", columnList = "quest_number"),
           @Index(name = "idx_quest_published", columnList = "published"),
           @Index(name = "idx_quest_difficulty", columnList = "difficulty"),
           @Index(name = "idx_quest_type", columnList = "quest_type"),
           @Index(name = "idx_quest_start_at", columnList = "start_at"),
           @Index(name = "idx_quest_end_at", columnList = "end_at"),
           @Index(name = "idx_quest_created_at", columnList = "created_at"),
           @Index(name = "idx_quest_updated_at", columnList = "updated_at"),
           @Index(name = "idx_quest_average_rating", columnList = "average_rating")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_quest_number", columnNames = {"quest_number"})
       })
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальный номер квеста для идентификации
     */
    @Column(name = "quest_number")
    private Long number;

    /**
     * Название квеста
     */
    @NotBlank(message = "Название квеста не может быть пустым")
    @Size(min = 3, max = 300, message = "Название квеста должно содержать от 3 до 300 символов")
    @Column(name = "title", nullable = false, length = 300)
    private String title;

    /**
     * HTML описание квеста
     */
    @Lob
    @Column(name = "description_html", columnDefinition = "TEXT")
    private String descriptionHtml;

    /**
     * Сложность квеста
     */
    @NotNull(message = "Сложность квеста обязательна")
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 16)
    private Difficulty difficulty;

    /**
     * Тип квеста (соло или командный)
     */
    @NotNull(message = "Тип квеста обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "quest_type", nullable = false, length = 8)
    private QuestType questType;

    /**
     * ID авторов квеста (в микросервисной архитектуре храним только ID)
     */
    @ElementCollection
    @CollectionTable(name = "quest_authors", 
                    joinColumns = @JoinColumn(name = "quest_id"),
                    uniqueConstraints = @UniqueConstraint(name = "uk_quest_author", columnNames = {"quest_id", "author_id"}))
    @Column(name = "author_id", nullable = false)
    @Builder.Default
    private Set<Long> authorIds = new HashSet<>();

    /**
     * Дата начала квеста
     */
    @Column(name = "start_at")
    private Instant startAt;

    /**
     * Дата окончания квеста
     */
    @Column(name = "end_at")
    private Instant endAt;

    /**
     * Опубликован ли квест
     */
    @Column(name = "published", nullable = false)
    @Builder.Default
    private Boolean published = false;

    /**
     * Статус квеста
     */
    @NotNull(message = "Статус квеста обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private QuestStatus status = QuestStatus.DRAFT;

    /**
     * Теги квеста для поиска и фильтрации
     */
    @ElementCollection
    @CollectionTable(name = "quest_tags", joinColumns = @JoinColumn(name = "quest_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    /**
     * Категория квеста
     */
    @Size(max = 100, message = "Категория не должна превышать 100 символов")
    @Column(name = "category", length = 100)
    private String category;

    /**
     * Максимальное количество участников (для командных квестов)
     */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    /**
     * Минимальное количество участников (для командных квестов)
     */
    @Column(name = "min_participants")
    private Integer minParticipants;

    /**
     * Оценочное время прохождения в минутах
     */
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    /**
     * URL изображения квеста
     */
    @Size(max = 500, message = "URL изображения не должен превышать 500 символов")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Версия квеста для управления версиями
     */
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * ID родительского квеста (для копий и версий)
     */
    @Column(name = "parent_quest_id")
    private Long parentQuestId;

    /**
     * Является ли квест шаблоном
     */
    @Column(name = "is_template", nullable = false)
    @Builder.Default
    private Boolean isTemplate = false;

    /**
     * Дата создания
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Дата обновления
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    /**
     * Дата публикации
     */
    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * ID пользователя, создавшего квест
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * ID пользователя, обновившего квест
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Архивирован ли квест
     */
    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    /**
     * Дата архивации
     */
    @Column(name = "archived_at")
    private Instant archivedAt;

    /**
     * Причина архивации
     */
    @Size(max = 500, message = "Причина архивации не должна превышать 500 символов")
    @Column(name = "archive_reason", length = 500)
    private String archiveReason;

    /**
     * Средний рейтинг квеста
     */
    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private Double averageRating = 0.0;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Проверяет, активен ли квест в текущий момент
     */
    public boolean isActive() {
        Instant now = Instant.now();
        return published && !archived && 
               (startAt == null || !startAt.isAfter(now)) && 
               (endAt == null || !endAt.isBefore(now));
    }

    /**
     * Проверяет, можно ли редактировать квест
     */
    public boolean isEditable() {
        return !published || status == QuestStatus.DRAFT;
    }

    /**
     * Проверяет, является ли квест командным
     */
    public boolean isTeamQuest() {
        return questType != null && questType.isTeam();
    }

    /**
     * Проверяет, является ли квест соло
     */
    public boolean isSoloQuest() {
        return questType != null && questType.isSolo();
    }

    /**
     * Добавляет автора квеста
     */
    public void addAuthor(Long authorId) {
        if (authorId != null) {
            authorIds.add(authorId);
        }
    }

    /**
     * Удаляет автора квеста
     */
    public void removeAuthor(Long authorId) {
        authorIds.remove(authorId);
    }

    /**
     * Добавляет тег квесту
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            tags.add(tag.trim().toLowerCase());
        }
    }

    /**
     * Удаляет тег квеста
     */
    public void removeTag(String tag) {
        if (tag != null) {
            tags.remove(tag.trim().toLowerCase());
        }
    }

    /**
     * Публикует квест
     */
    public void publish() {
        this.published = true;
        this.publishedAt = Instant.now();
        if (status == QuestStatus.DRAFT) {
            this.status = QuestStatus.PUBLISHED;
        }
    }

    /**
     * Снимает квест с публикации
     */
    public void unpublish() {
        this.published = false;
        this.publishedAt = null;
        if (status == QuestStatus.PUBLISHED) {
            this.status = QuestStatus.DRAFT;
        }
    }

    /**
     * Архивирует квест
     */
    public void archive(String reason) {
        this.archived = true;
        this.archivedAt = Instant.now();
        this.archiveReason = reason;
        this.published = false;
    }

    /**
     * Разархивирует квест
     */
    public void unarchive() {
        this.archived = false;
        this.archivedAt = null;
        this.archiveReason = null;
    }
}