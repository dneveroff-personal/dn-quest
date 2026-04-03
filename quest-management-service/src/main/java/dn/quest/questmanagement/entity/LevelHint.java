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
import java.util.UUID;

/**
 * Сущность подсказки уровня
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "level_hints",
       indexes = {
           @Index(name = "idx_hint_level_id", columnList = "level_id"),
           @Index(name = "idx_hint_order_index", columnList = "order_index"),
           @Index(name = "idx_hint_offset_sec", columnList = "offset_sec"),
           @Index(name = "idx_hint_level_order", columnList = "level_id, order_index"),
           @Index(name = "idx_hint_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_hint_level_order", columnNames = {"level_id", "order_index"})
       })
public class LevelHint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID уровня, к которому относится подсказка
     */
    @NotNull(message = "ID уровня обязателен")
    @Column(name = "level_id", nullable = false)
    private UUID levelId;

    /**
     * Порядковый номер подсказки в уровне
     */
    @NotNull(message = "Порядковый номер подсказки обязателен")
    @Min(value = 1, message = "Порядковый номер должен быть положительным")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /**
     * Смещение от начала уровня в секундах, после которого подсказка становится доступной
     */
    @NotNull(message = "Смещение времени обязательно")
    @Min(value = 0, message = "Смещение времени не может быть отрицательным")
    @Column(name = "offset_sec", nullable = false)
    @Builder.Default
    private Integer offsetSec = 0;

    /**
     * Текст подсказки (может содержать HTML)
     */
    @NotBlank(message = "Текст подсказки не может быть пустым")
    @Size(max = 2000, message = "Текст подсказки не должен превышать 2000 символов")
    @Lob
    @Column(name = "hint_text", nullable = false, columnDefinition = "TEXT")
    private String hintText;

    /**
     * Заголовок подсказки
     */
    @Size(max = 200, message = "Заголовок подсказки не должен превышать 200 символов")
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Тип подсказки
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "hint_type", nullable = false, length = 20)
    @Builder.Default
    private HintType hintType = HintType.TEXT;

    /**
     * URL изображения или файла подсказки
     */
    @Size(max = 500, message = "URL файла не должен превышать 500 символов")
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /**
     * Тип файла подсказки
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 20)
    private FileType fileType;

    /**
     * Стоимость подсказки в баллах или секундах
     */
    @Min(value = 0, message = "Стоимость подсказки не может быть отрицательной")
    @Column(name = "cost")
    @Builder.Default
    private Integer cost = 0;

    /**
     * Активна ли подсказка
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Обязательна ли подсказка (показывается автоматически)
     */
    @Column(name = "mandatory", nullable = false)
    @Builder.Default
    private Boolean mandatory = false;

    /**
     * Количество раз, когда подсказка была использована
     */
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

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
     * ID пользователя, создавшего подсказку
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * ID пользователя, обновившего подсказку
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Версия подсказки для оптимистичной блокировки
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
     * Проверяет, является ли подсказка текстовой
     */
    public boolean isTextHint() {
        return hintType == HintType.TEXT;
    }

    /**
     * Проверяет, является ли подсказка файловой
     */
    public boolean isFileHint() {
        return hintType == HintType.FILE;
    }

    /**
     * Проверяет, является ли подсказка изображением
     */
    public boolean isImageHint() {
        return hintType == HintType.FILE && fileType == FileType.IMAGE;
    }

    /**
     * Проверяет, является ли подсказка аудио
     */
    public boolean isAudioHint() {
        return hintType == HintType.FILE && fileType == FileType.AUDIO;
    }

    /**
     * Проверяет, является ли подсказка видео
     */
    public boolean isVideoHint() {
        return hintType == HintType.FILE && fileType == FileType.VIDEO;
    }

    /**
     * Проверяет, является ли подсказка документом
     */
    public boolean isDocumentHint() {
        return hintType == HintType.FILE && fileType == FileType.DOCUMENT;
    }

    /**
     * Проверяет, бесплатная ли подсказка
     */
    public boolean isFree() {
        return cost == null || cost == 0;
    }

    /**
     * Проверяет, платная ли подсказка
     */
    public boolean isPaid() {
        return !isFree();
    }

    /**
     * Увеличивает счетчик использований подсказки
     */
    public void incrementUsageCount() {
        if (usageCount == null) {
            usageCount = 0;
        }
        usageCount++;
    }

    /**
     * Получает копию подсказки с новыми параметрами
     */
    public LevelHint copy() {
        return LevelHint.builder()
                .levelId(this.levelId)
                .orderIndex(this.orderIndex)
                .offsetSec(this.offsetSec)
                .hintText(this.hintText)
                .title(this.title)
                .hintType(this.hintType)
                .fileUrl(this.fileUrl)
                .fileType(this.fileType)
                .cost(this.cost)
                .active(this.active)
                .mandatory(this.mandatory)
                .build();
    }

    /**
     * Обновляет подсказку из другой подсказки
     */
    public void updateFrom(LevelHint other) {
        this.orderIndex = other.getOrderIndex();
        this.offsetSec = other.getOffsetSec();
        this.hintText = other.getHintText();
        this.title = other.getTitle();
        this.hintType = other.getHintType();
        this.fileUrl = other.getFileUrl();
        this.fileType = other.getFileType();
        this.cost = other.getCost();
        this.active = other.getActive();
        this.mandatory = other.getMandatory();
    }

    /**
     * Валидирует подсказку перед сохранением
     */
    public boolean isValid() {
        // Для файловых подсказок должен быть указан URL файла
        if (isFileHint() && (fileUrl == null || fileUrl.trim().isEmpty())) {
            return false;
        }
        
        // Для файловых подсказок должен быть указан тип файла
        if (isFileHint() && fileType == null) {
            return false;
        }
        
        // Для текстовых подсказок не должен быть указан URL файла
        if (isTextHint() && fileUrl != null && !fileUrl.trim().isEmpty()) {
            return false;
        }
        
        // Текст подсказки не должен быть пустым
        if (hintText == null || hintText.trim().isEmpty()) {
            return false;
        }
        
        // Смещение времени не должно быть отрицательным
        if (offsetSec != null && offsetSec < 0) {
            return false;
        }
        
        // Стоимость не должна быть отрицательной
        return cost == null || cost >= 0;
    }

    /**
     * Типы подсказок
     */
    public enum HintType {
        TEXT("Текст"),
        FILE("Файл");

        private final String displayName;

        HintType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Типы файлов подсказок
     */
    public enum FileType {
        IMAGE("Изображение"),
        AUDIO("Аудио"),
        VIDEO("Видео"),
        DOCUMENT("Документ");

        private final String displayName;

        FileType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}