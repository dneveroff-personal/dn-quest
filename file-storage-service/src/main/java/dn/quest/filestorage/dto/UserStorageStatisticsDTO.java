package dn.quest.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO для статистики хранилища пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStorageStatisticsDTO {

    /**
     * ID пользователя
     */
    private UUID userId;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Общее количество файлов пользователя
     */
    private Long totalFiles;

    /**
     * Общий размер файлов пользователя в байтах
     */
    private Long totalSizeBytes;

    /**
     * Общий размер в человекочитаемом формате
     */
    private String formattedTotalSize;

    /**
     * Количество файлов по типам
     */
    private Map<String, Long> filesByType;

    /**
     * Размер файлов по типам в байтах
     */
    private Map<String, Long> sizeByType;

    /**
     * Количество публичных файлов
     */
    private Long publicFiles;

    /**
     * Количество временных файлов
     */
    private Long temporaryFiles;

    /**
     * Размер временных файлов в байтах
     */
    private Long temporaryFilesSizeBytes;

    /**
     * Размер временных файлов в человекочитаемом формате
     */
    private String formattedTemporaryFilesSize;

    /**
     * Общее количество скачиваний файлов пользователя
     */
    private Long totalDownloads;

    /**
     * Самый популярный файл
     */
    private FileResponseDTO mostPopularFile;

    /**
     * Самый большой файл
     */
    private FileResponseDTO largestFile;

    /**
     * Последний загруженный файл
     */
    private FileResponseDTO lastUploadedFile;

    /**
     * Средний размер файла
     */
    private Double averageFileSizeBytes;

    /**
     * Средний размер файла в человекочитаемом формате
     */
    private String formattedAverageFileSize;

    /**
     * Лимит хранилища для пользователя (если есть)
     */
    private Long storageLimitBytes;

    /**
     * Лимит хранилища в человекочитаемом формате
     */
    private String formattedStorageLimit;

    /**
     * Использование лимита в процентах
     */
    private Double limitUsagePercentage;

    /**
     * Дата первого загрузки файла
     */
    private java.time.LocalDateTime firstUploadDate;

    /**
     * Дата последнего загрузки файла
     */
    private java.time.LocalDateTime lastUploadDate;

    /**
     * Количество дней использования хранилища
     */
    private Long daysOfUsage;

    /**
     * Получить отформатированный размер
     */
    public static String formatSize(Long sizeBytes) {
        if (sizeBytes == null || sizeBytes == 0) {
            return "0 B";
        }
        return FileResponseDTO.formatFileSize(sizeBytes);
    }

    /**
     * Рассчитать процент использования лимита
     */
    public Double calculateLimitUsagePercentage() {
        if (storageLimitBytes == null || storageLimitBytes == 0 || totalSizeBytes == null) {
            return 0.0;
        }
        return (double) totalSizeBytes / storageLimitBytes * 100;
    }

    /**
     * Проверить, превышен ли лимит
     */
    public boolean isLimitExceeded() {
        return limitUsagePercentage != null && limitUsagePercentage > 100;
    }

    /**
     * Проверить, близок ли лимит к превышению
     */
    public boolean isLimitNearlyExceeded() {
        return limitUsagePercentage != null && limitUsagePercentage > 90;
    }
}