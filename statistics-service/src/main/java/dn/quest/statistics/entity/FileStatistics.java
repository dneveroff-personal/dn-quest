package dn.quest.statistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность для хранения файловой статистики
 */
@Entity
@Table(name = "file_statistics", indexes = {
    @Index(name = "idx_file_statistics_file_id", columnList = "fileId"),
    @Index(name = "idx_file_statistics_date", columnList = "date"),
    @Index(name = "idx_file_statistics_owner_id", columnList = "ownerId"),
    @Index(name = "idx_file_statistics_file_date", columnList = "fileId, date"),
    @Index(name = "idx_file_statistics_owner_date", columnList = "ownerId, date"),
    @Index(name = "idx_file_statistics_entity_type", columnList = "entityType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID файла
     */
    @Column(name = "file_id", nullable = false)
    private String fileId;

    /**
     * ID владельца файла
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * ID сущности, к которой привязан файл
     */
    @Column(name = "entity_id")
    private String entityId;

    /**
     * Тип сущности (quest, user, team, etc.)
     */
    @Column(name = "entity_type")
    private String entityType;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Количество загрузок файлов (для агрегированных записей)
     */
    @Column(name = "uploads")
    private Integer uploads;

    /**
     * Количество обновлений файлов
     */
    @Column(name = "updates")
    private Integer updates;

    /**
     * Количество удалений файлов
     */
    @Column(name = "deletions")
    private Integer deletions;

    /**
     * Количество скачиваний файла
     */
    @Column(name = "downloads")
    private Integer downloads;

    /**
     * Количество уникальных скачиваний
     */
    @Column(name = "unique_downloads")
    private Integer uniqueDownloads;

    /**
     * Количество просмотров файла
     */
    @Column(name = "views")
    private Integer views;

    /**
     * Количество уникальных просмотров
     */
    @Column(name = "unique_views")
    private Integer uniqueViews;

    /**
     * Размер файла в байтах
     */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /**
     * Общий размер загруженных файлов (для агрегированных записей)
     */
    @Column(name = "total_size_bytes")
    private Long totalSizeBytes;

    /**
     * MIME тип файла
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * Тип файла (image, document, video, audio, archive, other)
     */
    @Column(name = "file_type")
    private String fileType;

    /**
     * Расширение файла
     */
    @Column(name = "file_extension")
    private String fileExtension;

    /**
     * Публичный ли файл
     */
    @Column(name = "is_public")
    private Boolean isPublic;

    /**
     * Количество лайков
     */
    @Column(name = "likes_count")
    private Integer likesCount;

    /**
     * Количество комментариев
     */
    @Column(name = "comments_count")
    private Integer commentsCount;

    /**
     * Количество репостов
     */
    @Column(name = "shares_count")
    private Integer sharesCount;

    /**
     * Количество добавлений в избранное
     */
    @Column(name = "favorites_count")
    private Integer favoritesCount;

    /**
     * Хеш файла (MD5 или SHA256)
     */
    @Column(name = "file_hash")
    private String fileHash;

    /**
     * URL для доступа к файлу
     */
    @Column(name = "file_url")
    private String fileUrl;

    /**
     * IP адрес загрузки
     */
    @Column(name = "upload_ip")
    private String uploadIp;

    /**
     * User Agent при загрузке
     */
    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Устройство загрузки
     */
    @Column(name = "device_type")
    private String deviceType;

    /**
     * Браузер загрузки
     */
    @Column(name = "browser")
    private String browser;

    /**
     * Операционная система загрузки
     */
    @Column(name = "operating_system")
    private String operatingSystem;

    /**
     * Время загрузки файла
     */
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    /**
     * Время последнего доступа
     */
    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    /**
     * Количество предписанных URL сгенерировано
     */
    @Column(name = "presigned_urls_generated")
    private Integer presignedUrlsGenerated;

    /**
     * Общее время жизни предписанных URL (в часах)
     */
    @Column(name = "total_presigned_url_hours")
    private Long totalPresignedUrlHours;

    /**
     * Метаданные файла (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}