package dn.quest.shared.events.file;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие обновления файла
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileUpdatedEvent extends BaseEvent {

    /**
     * ID файла
     */
    private String fileId;

    /**
     * Оригинальное имя файла (если изменилось)
     */
    private String originalFileName;

    /**
     * Путь к файлу в хранилище (если изменился)
     */
    private String filePath;

    /**
     * Размер файла в байтах (если изменился)
     */
    private Long fileSizeBytes;

    /**
     * MIME тип файла (если изменился)
     */
    private String mimeType;

    /**
     * Тип файла (если изменился)
     */
    private String fileType;

    /**
     * ID владельца файла (если изменился)
     */
    private String ownerId;

    /**
     * Имя владельца файла (если изменилось)
     */
    private String ownerName;

    /**
     * ID сущности, к которой привязан файл (если изменился)
     */
    private String entityId;

    /**
     * Тип сущности (если изменился)
     */
    private String entityType;

    /**
     * Время обновления
     */
    private java.time.Instant updatedAt;

    /**
     * IP адрес обновления
     */
    private String updateIp;

    /**
     * User Agent при обновлении
     */
    private String userAgent;

    /**
     * ID пользователя, обновившего файл
     */
    private String updatedBy;

    /**
     * Имя пользователя, обновившего файл
     */
    private String updatedByName;

    /**
     * Публичный ли файл (если изменилось)
     */
    private Boolean isPublic;

    /**
     * URL для доступа к файлу (если изменился)
     */
    private String fileUrl;

    /**
     * Метаданные файла (если изменились)
     */
    private Map<String, Object> fileMetadata;

    /**
     * Список измененных полей
     */
    private java.util.List<String> changedFields;

    /**
     * Создание события обновления файла
     */
    public static FileUpdatedEvent create(String fileId, String originalFileName,
                                        String filePath, Long fileSizeBytes,
                                        String mimeType, String fileType,
                                        String ownerId, String ownerName,
                                        String entityId, String entityType,
                                        java.time.Instant updatedAt, String updateIp,
                                        String userAgent, String updatedBy,
                                        String updatedByName, Boolean isPublic,
                                        String fileUrl, Map<String, Object> fileMetadata,
                                        java.util.List<String> changedFields,
                                        String correlationId) {
        return FileUpdatedEvent.builder()
                .eventType("FileUpdated")
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
                .updatedAt(updatedAt)
                .updateIp(updateIp)
                .userAgent(userAgent)
                .updatedBy(updatedBy)
                .updatedByName(updatedByName)
                .isPublic(isPublic)
                .fileUrl(fileUrl)
                .fileMetadata(fileMetadata)
                .changedFields(changedFields)
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
                        "updatedAt", updatedAt.toString(),
                        "updateIp", updateIp,
                        "userAgent", userAgent,
                        "updatedBy", updatedBy,
                        "updatedByName", updatedByName,
                        "isPublic", isPublic,
                        "fileUrl", fileUrl,
                        "fileMetadata", fileMetadata,
                        "changedFields", changedFields
                ))
                .build();
    }
}