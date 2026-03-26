package dn.quest.questmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import dn.quest.shared.enums.FileType;

/**
 * Сущность медиа файла квеста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quest_media",
       indexes = {
           @Index(name = "idx_media_quest_id", columnList = "quest_id"),
           @Index(name = "idx_media_level_id", columnList = "level_id"),
           @Index(name = "idx_media_type", columnList = "media_type"),
           @Index(name = "idx_media_file_type", columnList = "file_type"),
           @Index(name = "idx_media_quest_type", columnList = "quest_id, media_type"),
           @Index(name = "idx_media_level_type", columnList = "level_id, media_type"),
           @Index(name = "idx_media_created_at", columnList = "created_at")
       })
public class QuestMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID квеста (null если медиа относится к уровню)
     */
    @Column(name = "quest_id")
    private Long questId;

    /**
     * ID уровня (null если медиа относится к квесту)
     */
    @Column(name = "level_id")
    private Long levelId;

    /**
     * Тип медиа
     */
    @NotNull(message = "Тип медиа обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    /**
     * Тип файла
     */
    @NotNull(message = "Тип файла обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType;

    /**
     * Оригинальное имя файла
     */
    @NotBlank(message = "Имя файла не может быть пустым")
    @Size(max = 255, message = "Имя файла не должно превышать 255 символов")
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Путь к файлу в хранилище
     */
    @NotBlank(message = "Путь к файлу не может быть пустым")
    @Size(max = 1000, message = "Путь к файлу не должен превышать 1000 символов")
    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    /**
     * URL файла для доступа
     */
    @Size(max = 1000, message = "URL файла не должен превышать 1000 символов")
    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    /**
     * Размер файла в байтах
     */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /**
     * MIME тип файла
     */
    @Size(max = 100, message = "MIME тип не должен превышать 100 символов")
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * Заголовок медиа
     */
    @Size(max = 200, message = "Заголовок не должен превышать 200 символов")
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Описание медиа
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Порядковый номер медиа (для сортировки)
     */
    @Column(name = "order_index")
    private Integer orderIndex;

    /**
     * Активно ли медиа
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Является ли медиа обложкой
     */
    @Column(name = "is_cover", nullable = false)
    @Builder.Default
    private Boolean isCover = false;

    /**
     * Ширина изображения (для изображений)
     */
    @Column(name = "width")
    private Integer width;

    /**
     * Высота изображения (для изображений)
     */
    @Column(name = "height")
    private Integer height;

    /**
     * Длительность видео/аудио в секундах
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

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
     * ID пользователя, загрузившего медиа
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * ID пользователя, обновившего медиа
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Версия медиа для оптимистичной блокировки
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
     * Проверяет, относится ли медиа к квесту
     */
    public boolean isQuestMedia() {
        return questId != null && levelId == null;
    }

    /**
     * Проверяет, относится ли медиа к уровню
     */
    public boolean isLevelMedia() {
        return levelId != null;
    }

    /**
     * Проверяет, является ли медиа изображением
     */
    public boolean isImage() {
        return fileType == FileType.IMAGE;
    }

    /**
     * Проверяет, является ли медиа видео
     */
    public boolean isVideo() {
        return fileType == FileType.VIDEO;
    }

    /**
     * Проверяет, является ли медиа аудио
     */
    public boolean isAudio() {
        return fileType == FileType.AUDIO;
    }

    /**
     * Проверяет, является ли медиа документом
     */
    public boolean isDocument() {
        return fileType == FileType.DOCUMENT;
    }

    /**
     * Проверяет, является ли медиа архивом
     */
    public boolean isArchive() {
        return fileType == FileType.ARCHIVE;
    }

    /**
     * Проверяет, есть ли у медиа размеры
     */
    public boolean hasDimensions() {
        return width != null && height != null;
    }

    /**
     * Проверяет, есть ли у медиа длительность
     */
    public boolean hasDuration() {
        return durationSeconds != null && durationSeconds > 0;
    }

    /**
     * Получает размер файла в человекочитаемом формате
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) {
            return "Unknown";
        }

        long bytes = fileSizeBytes;
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Получает копию медиа с новыми параметрами
     */
    public QuestMedia copy() {
        return QuestMedia.builder()
                .questId(this.questId)
                .levelId(this.levelId)
                .mediaType(this.mediaType)
                .fileType(this.fileType)
                .originalFilename(this.originalFilename)
                .filePath(this.filePath)
                .fileUrl(this.fileUrl)
                .fileSizeBytes(this.fileSizeBytes)
                .mimeType(this.mimeType)
                .title(this.title)
                .description(this.description)
                .orderIndex(this.orderIndex)
                .active(this.active)
                .isCover(this.isCover)
                .width(this.width)
                .height(this.height)
                .durationSeconds(this.durationSeconds)
                .build();
    }

    /**
     * Обновляет медиа из другого медиа
     */
    public void updateFrom(QuestMedia other) {
        this.mediaType = other.getMediaType();
        this.fileType = other.getFileType();
        this.originalFilename = other.getOriginalFilename();
        this.filePath = other.getFilePath();
        this.fileUrl = other.getFileUrl();
        this.fileSizeBytes = other.getFileSizeBytes();
        this.mimeType = other.getMimeType();
        this.title = other.getTitle();
        this.description = other.getDescription();
        this.orderIndex = other.getOrderIndex();
        this.active = other.getActive();
        this.isCover = other.getIsCover();
        this.width = other.getWidth();
        this.height = other.getHeight();
        this.durationSeconds = other.getDurationSeconds();
    }

    /**
     * Валидирует медиа перед сохранением
     */
    public boolean isValid() {
        // Должен быть указан либо квест, либо уровень, но не оба одновременно
        if ((questId == null && levelId == null) || (questId != null && levelId != null)) {
            return false;
        }

        // Имя файла не должно быть пустым
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return false;
        }

        // Путь к файлу не должен быть пустым
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        // Размер файла должен быть положительным
        if (fileSizeBytes != null && fileSizeBytes <= 0) {
            return false;
        }

        // Размеры должны быть положительными
        if ((width != null && width <= 0) || (height != null && height <= 0)) {
            return false;
        }

        // Длительность должна быть положительной
        if (durationSeconds != null && durationSeconds <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Типы медиа
     */
    public enum MediaType {
        IMAGE("Изображение"),
        VIDEO("Видео"),
        AUDIO("Аудио"),
        DOCUMENT("Документ"),
        ARCHIVE("Архив");

        private final String displayName;

        MediaType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
