package dn.quest.filestorage.dto;

import dn.quest.filestorage.entity.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO для ответа с информацией о файле
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDTO {

    /**
     * ID файла
     */
    private java.util.UUID id;

    /**
     * Оригинальное имя файла
     */
    private String originalFileName;

    /**
     * MIME тип файла
     */
    private String contentType;

    /**
     * Размер файла в байтах
     */
    private Long fileSize;

    /**
     * Размер файла в человекочитаемом формате
     */
    private String formattedFileSize;

    /**
     * Тип файла
     */
    private FileMetadata.FileType fileType;

    /**
     * Тип хранилища
     */
    private FileMetadata.StorageType storageType;

    /**
     * Описание файла
     */
    private String description;

    /**
     * ID владельца файла
     */
    private UUID ownerId;

    /**
     * Является ли файл публичным
     */
    private Boolean isPublic;

    /**
     * Является ли файл временным
     */
    private Boolean isTemporary;

    /**
     * Время истечения для временных файлов
     */
    private LocalDateTime expiresAt;

    /**
     * Количество скачиваний
     */
    private Long downloadCount;

    /**
     * Время последнего доступа
     */
    private LocalDateTime lastAccessedAt;

    /**
     * Контрольная сумма файла
     */
    private String checksum;

    /**
     * Путь к миниатюре
     */
    private String thumbnailPath;

    /**
     * Дополнительные метаданные
     */
    private Map<String, String> metadata;

    /**
     * Время создания
     */
    private LocalDateTime createdAt;

    /**
     * Время обновления
     */
    private LocalDateTime updatedAt;

    /**
     * URL для скачивания файла
     */
    private String downloadUrl;

    /**
     * URL для просмотра файла (если применимо)
     */
    private String viewUrl;

    /**
     * URL миниатюры
     */
    private String thumbnailUrl;

    /**
     * Оставшееся время жизни для временных файлов
     */
    private String timeToExpiration;

    /**
     * Статус файла
     */
    private FileStatus status;

    /**
     * Статусы файла
     */
    public enum FileStatus {
        ACTIVE("Активен"),
        EXPIRED("Истек"),
        DELETED("Удален"),
        PROCESSING("Обрабатывается");

        private final String description;

        FileStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Получить отформатированный размер файла
     */
    public static String formatFileSize(long bytes) {
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
     * Получить время до истечения
     */
    public static String getTimeToExpiration(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (expiresAt.isBefore(now)) {
            return "Истек";
        }

        java.time.Duration duration = java.time.Duration.between(now, expiresAt);
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        if (days > 0) {
            return String.format("%d д %d ч", days, hours);
        } else if (hours > 0) {
            return String.format("%d ч %d мин", hours, minutes);
        } else {
            return String.format("%d мин", minutes);
        }
    }

    /**
     * Определить статус файла на основе метаданных
     */
    public static FileStatus determineStatus(FileMetadata metadata) {
        if (metadata.getIsTemporary() && metadata.getExpiresAt() != null 
            && metadata.getExpiresAt().isBefore(LocalDateTime.now())) {
            return FileStatus.EXPIRED;
        }
        return FileStatus.ACTIVE;
    }
}