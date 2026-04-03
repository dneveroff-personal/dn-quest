package dn.quest.filestorage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность для хранения метаданных файлов
 */
@Entity
@Table(name = "file_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 255)
    private String storedFileName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Column(length = 500)
    private String storagePath;

    @Column(length = 1000)
    private String description;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "quest_id")
    private UUID questId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "is_temporary")
    @Builder.Default
    private Boolean isTemporary = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Типы файлов
     */
    public enum FileType {
        AVATAR("Аватар пользователя"),
        QUEST_MEDIA("Медиа файл квеста"),
        LEVEL_FILE("Файл уровня"),
        TEMPORARY("Временный файл"),
        DOCUMENT("Документ"),
        IMAGE("Изображение"),
        VIDEO("Видео"),
        AUDIO("Аудио"),
        OTHER("Другое");

        private final String description;

        FileType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Типы хранилищ
     */
    public enum StorageType {
        LOCAL("Локальное хранилище"),
        MINIO("MinIO"),
        S3("AWS S3"),
        CDN("CDN");

        private final String description;

        StorageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}