package dn.quest.shared.events.file;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие загрузки файла
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileUploadedEvent extends BaseEvent {

    /**
     * ID файла
     */
    private String fileId;

    /**
     * Оригинальное имя файла
     */
    private String originalFileName;

    /**
     * Имя файла в хранилище
     */
    private String storageFileName;

    /**
     * Путь к файлу в хранилище
     */
    private String filePath;

    /**
     * Размер файла в байтах
     */
    private Long fileSizeBytes;

    /**
     * MIME тип файла
     */
    private String mimeType;

    /**
     * Тип файла (image, document, video, etc.)
     */
    private String fileType;

    /**
     * ID владельца файла
     */
    private String ownerId;

    /**
     * Имя владельца файла
     */
    private String ownerName;

    /**
     * ID сущности, к которой привязан файл
     */
    private String entityId;

    /**
     * Тип сущности (quest, user, team, etc.)
     */
    private String entityType;

    /**
     * Время загрузки
     */
    private java.time.Instant uploadedAt;

    /**
     * IP адрес загрузки
     */
    private String uploadIp;

    /**
     * User Agent при загрузке
     */
    private String userAgent;

    /**
     * Хеш файла (MD5 или SHA256)
     */
    private String fileHash;

    /**
     * Публичный ли файл
     */
    private Boolean isPublic;

    /**
     * URL для доступа к файлу
     */
    private String fileUrl;

    /**
     * Метаданные файла
     */
    private Map<String, Object> fileMetadata;

    /**
     * Создание события загрузки файла
     */
    public static FileUploadedEvent create(String fileId, String originalFileName,
                                         String storageFileName, String filePath,
                                         Long fileSizeBytes, String mimeType,
                                         String fileType, String ownerId,
                                         String ownerName, String entityId,
                                         String entityType, java.time.Instant uploadedAt,
                                         String uploadIp, String userAgent,
                                         String fileHash, Boolean isPublic,
                                         String fileUrl, Map<String, Object> fileMetadata,
                                         String correlationId) {
        return FileUploadedEvent.builder()
                .eventType("FileUploaded")
                .eventVersion("1.0")
                .source("file-storage-service")
                .correlationId(correlationId)
                .fileId(fileId)
                .originalFileName(originalFileName)
                .storageFileName(storageFileName)
                .filePath(filePath)
                .fileSizeBytes(fileSizeBytes)
                .mimeType(mimeType)
                .fileType(fileType)
                .ownerId(ownerId)
                .ownerName(ownerName)
                .entityId(entityId)
                .entityType(entityType)
                .uploadedAt(uploadedAt)
                .uploadIp(uploadIp)
                .userAgent(userAgent)
                .fileHash(fileHash)
                .isPublic(isPublic)
                .fileUrl(fileUrl)
                .fileMetadata(fileMetadata)
                .data(Map.of(
                        "fileId", fileId,
                        "originalFileName", originalFileName,
                        "storageFileName", storageFileName,
                        "filePath", filePath,
                        "fileSizeBytes", fileSizeBytes,
                        "mimeType", mimeType,
                        "fileType", fileType,
                        "ownerId", ownerId,
                        "ownerName", ownerName,
                        "entityId", entityId,
                        "entityType", entityType,
                        "uploadedAt", uploadedAt.toString(),
                        "uploadIp", uploadIp,
                        "userAgent", userAgent,
                        "fileHash", fileHash,
                        "isPublic", isPublic,
                        "fileUrl", fileUrl,
                        "fileMetadata", fileMetadata
                ))
                .build();
    }
}