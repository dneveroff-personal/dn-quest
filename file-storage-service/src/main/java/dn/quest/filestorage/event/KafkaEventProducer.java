package dn.quest.filestorage.event;

import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import dn.quest.filestorage.entity.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Сервис для публикации событий в Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final EventProducer eventProducer;

    /**
     * Публикация события загрузки файла
     */
    public void publishFileUploadedEvent(FileMetadata fileMetadata) {
        FileUploadedEvent event = FileUploadedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .fileId(fileMetadata.getId() != null ? fileMetadata.getId().getMostSignificantBits() : null)
                .userId(fileMetadata.getOwnerId() != null ? fileMetadata.getOwnerId().getMostSignificantBits() : null)
                .fileName(fileMetadata.getStoredFileName())
                .originalFileName(fileMetadata.getOriginalFileName())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .filePath(fileMetadata.getStoragePath())
                .storageType(fileMetadata.getStorageType() != null ? fileMetadata.getStorageType().name() : null)
                .build();
        eventProducer.publishFileUploadedEvent(event);
        log.info("Published file uploaded event for file: {}", fileMetadata.getId());
    }

    /**
     * Публикация события обновления файла
     */
    public void publishFileUpdatedEvent(FileMetadata fileMetadata) {
        FileUpdatedEvent event = FileUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .fileId(fileMetadata.getId() != null ? fileMetadata.getId().getMostSignificantBits() : null)
                .userId(fileMetadata.getOwnerId() != null ? fileMetadata.getOwnerId().getMostSignificantBits() : null)
                .fileName(fileMetadata.getStoredFileName())
                .originalFileName(fileMetadata.getOriginalFileName())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .filePath(fileMetadata.getStoragePath())
                .storageType(fileMetadata.getStorageType() != null ? fileMetadata.getStorageType().name() : null)
                .build();
        eventProducer.publishFileUpdatedEvent(event);
        log.info("Published file updated event for file: {}", fileMetadata.getId());
    }

    /**
     * Публикация события удаления файла
     */
    public void publishFileDeletedEvent(Long fileId, Long userId, String fileName) {
        FileDeletedEvent event = FileDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .fileId(fileId)
                .userId(userId)
                .fileName(fileName)
                .build();
        eventProducer.publishFileDeletedEvent(event);
        log.info("Published file deleted event for file: {}", fileId);
    }

    /**
     * Публикация события уведомления о файле
     */
    public void publishFileNotificationEvent(Long userId, String title, String message, String type) {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
                userId,
                title,
                message,
                type
        );
        eventProducer.publishNotificationEvent(event);
        log.info("Published file notification event for user: {}", userId);
    }

    /**
     * Публикация события об успешной загрузке файла
     */
    public void publishFileUploadSuccessEvent(FileMetadata fileMetadata) {
        publishFileUploadedEvent(fileMetadata);
        if (fileMetadata.getOwnerId() != null) {
            publishFileNotificationEvent(
                    fileMetadata.getOwnerId().getMostSignificantBits(),
                    "Файл загружен",
                    String.format("Файл '%s' успешно загружен", fileMetadata.getOriginalFileName()),
                    "FILE_UPLOAD_SUCCESS"
            );
        }
    }

    /**
     * Публикация события об ошибке загрузки файла
     */
    public void publishFileUploadErrorEvent(Long userId, String fileName, String errorMessage) {
        publishFileNotificationEvent(
                userId,
                "Ошибка загрузки файла",
                String.format("Не удалось загрузить файл '%s': %s", fileName, errorMessage),
                "FILE_UPLOAD_ERROR"
        );
    }

    /**
     * Публикация события об успешном удалении файла
     */
    public void publishFileDeleteSuccessEvent(Long fileId, Long userId, String fileName) {
        publishFileDeletedEvent(fileId, userId, fileName);
        publishFileNotificationEvent(
                userId,
                "Файл удален",
                String.format("Файл '%s' успешно удален", fileName),
                "FILE_DELETE_SUCCESS"
        );
    }

    /**
     * Публикация события об ошибке удаления файла
     */
    public void publishFileDeleteErrorEvent(Long userId, String fileName, String errorMessage) {
        publishFileNotificationEvent(
                userId,
                "Ошибка удаления файла",
                String.format("Не удалось удалить файл '%s': %s", fileName, errorMessage),
                "FILE_DELETE_ERROR"
        );
    }
}