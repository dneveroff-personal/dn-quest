package dn.quest.questmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность уровня квеста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "levels",
       indexes = {
           @Index(name = "idx_level_quest_id", columnList = "quest_id"),
           @Index(name = "idx_level_order_index", columnList = "order_index"),
           @Index(name = "idx_level_quest_order", columnList = "quest_id, order_index"),
           @Index(name = "idx_level_created_at", columnList = "created_at"),
           @Index(name = "idx_level_updated_at", columnList = "updated_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_level_quest_order", columnNames = {"quest_id", "order_index"})
       })
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID квеста, к которому относится уровень
     */
    @NotNull(message = "ID квеста обязателен")
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * Порядковый номер уровня в квесте
     */
    @NotNull(message = "Порядковый номер уровня обязателен")
    @Min(value = 1, message = "Порядковый номер должен быть положительным")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /**
     * Название уровня
     */
    @NotBlank(message = "Название уровня не может быть пустым")
    @Size(min = 3, max = 200, message = "Название уровня должно содержать от 3 до 200 символов")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * HTML описание уровня
     */
    @Lob
    @Column(name = "description_html", columnDefinition = "TEXT")
    private String descriptionHtml;

    /**
     * Максимальное время на прохождение уровня в секундах (Auto-Pass Time)
     */
    @Min(value = 0, message = "Время на прохождение не может быть отрицательным")
    @Column(name = "ap_time")
    private Integer apTime;

    /**
     * Количество секторов, которые нужно закрыть для прохождения уровня
     */
    @NotNull(message = "Количество требуемых секторов обязательно")
    @Min(value = 0, message = "Количество требуемых секторов не может быть отрицательным")
    @Column(name = "required_sectors", nullable = false)
    @Builder.Default
    private Integer requiredSectors = 0;

    /**
     * Координаты уровня (для геолокационных квестов)
     */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    /**
     * Дополнительные параметры уровня в формате JSON
     */
    @Lob
    @Column(name = "additional_params", columnDefinition = "JSONB")
    private String additionalParams;

    /**
     * Активен ли уровень
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Дата создания
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата обновления
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * ID пользователя, создавшего уровень
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * ID пользователя, обновившего уровень
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Версия уровня для оптимистичной блокировки
     */
    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Проверяет, является ли уровень геолокационным
     */
    public boolean isGeolocationLevel() {
        return latitude != null && longitude != null;
    }

    /**
     * Проверяет, ограничено ли время на прохождение уровня
     */
    public boolean hasTimeLimit() {
        return apTime != null && apTime > 0;
    }


    /**
     * Получает копию уровня с новыми параметрами
     */
    public Level copy() {
        return Level.builder()
                .questId(this.questId)
                .orderIndex(this.orderIndex)
                .title(this.title)
                .descriptionHtml(this.descriptionHtml)
                .apTime(this.apTime)
                .requiredSectors(this.requiredSectors)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .additionalParams(this.additionalParams)
                .active(this.active)
                .build();
    }

    /**
     * Обновляет уровень из другого уровня
     */
    public void updateFrom(Level other) {
        this.title = other.getTitle();
        this.descriptionHtml = other.getDescriptionHtml();
        this.apTime = other.getApTime();
        this.requiredSectors = other.getRequiredSectors();
        this.latitude = other.getLatitude();
        this.longitude = other.getLongitude();
        this.additionalParams = other.getAdditionalParams();
        this.active = other.getActive();
    }
}