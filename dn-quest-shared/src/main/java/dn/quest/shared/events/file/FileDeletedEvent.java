package dn.quest.shared.events.file;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие удаления файла
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileDeletedEvent extends BaseEvent {

    /**
     * ID файла
     */
    private String fileId;

    /**
     * Оригинальное имя файла
     */
    private String originalFileName;

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
     * ID сущности, к которой был привязан файл
     */
    private String entityId;

    /**
     * Тип сущности (quest, user, team, etc.)
     */
    private String entityType;

    /**
     * Время удаления
     */
    private java.time.Instant deletedAt;

    /**
     * Причина удаления
     */
    private String deletionReason;

    /**
     * Тип удаления (soft, hard)
     */
    private String deletionType;

    /**
     * ID пользователя, удалившего файл
     */
    private String deletedBy;

    /**
     * Имя пользователя, удалившего файл
     */
    private String deletedByName;

    /**
     * IP адрес удаления
     */
    private String deletionIp;

    /**
     * User Agent при удалении
     */
    private String userAgent;

    /**
     * Хеш файла
     */
    private String fileHash;

    /**
     * Был ли файл публичным
     */
    private Boolean wasPublic;

    /**
     * URL для доступа к файлу (если был)
     */
    private String fileUrl;

    /**
     * Создание события удаления файла
     */
    public static FileDeletedEvent create(String fileId, String originalFileName,
                                        String filePath, Long fileSizeBytes,
                                        String mimeType, String fileType,
                                        String ownerId, String ownerName,
                                        String entityId, String entityType,
                                        java.time.Instant deletedAt, String deletionReason,
                                        String deletionType, String deletedBy,
                                        String deletedByName, String deletionIp,
                                        String userAgent, String fileHash,
                                        Boolean wasPublic, String fileUrl,
                                        String correlationId) {
        return FileDeletedEvent.builder()
                .eventType("FileDeleted")
                .eventVersion("1.0")
                .source("file-storage-service")
                .correlationId(correlationId)
                .fileId(fileId)
                .originalFileName(originalFileName)
                .filePath(filePath)
                .fileSizeBytes(fileSizeBytes)
                .mimeType(mimeType)
                .fileType(fileType)
                .ownerId(ownerId)
                .ownerName(ownerName)
                .entityId(entityId)
                .entityType(entityType)
                .deletedAt(deletedAt)
                .deletionReason(deletionReason)
                .deletionType(deletionType)
                .deletedBy(deletedBy)
                .deletedByName(deletedByName)
                .deletionIp(deletionIp)
                .userAgent(userAgent)
                .fileHash(fileHash)
                .wasPublic(wasPublic)
                .fileUrl(fileUrl)
                .data(Map.of(
                        "fileId", fileId,
                        "originalFileName", originalFileName,
                        "filePath", filePath,
                        "fileSizeBytes", fileSizeBytes,
                        "mimeType", mimeType,
                        "fileType", fileType,
                        "ownerId", ownerId,
                        "ownerName", ownerName,
                        "entityId", entityId,
                        "entityType", entityType,
                        "deletedAt", deletedAt.toString(),
                        "deletionReason", deletionReason,
                        "deletionType", deletionType,
                        "deletedBy", deletedBy,
                        "deletedByName", deletedByName,
                        "deletionIp", deletionIp,
                        "userAgent", userAgent,
                        "fileHash", fileHash,
                        "wasPublic", wasPublic,
                        "fileUrl", fileUrl
                ))
                .build();
    }
}