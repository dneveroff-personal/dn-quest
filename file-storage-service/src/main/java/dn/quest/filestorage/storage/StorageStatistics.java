package dn.quest.filestorage.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Статистика хранилища файлов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageStatistics {

    /**
     * Тип хранилища
     */
    private String storageType;

    /**
     * Общее количество файлов
     */
    private Long totalFiles;

    /**
     * Общий размер хранилища в байтах
     */
    private Long totalSizeBytes;

    /**
     * Доступное место в байтах
     */
    private Long availableSpaceBytes;

    /**
     * Использованное место в процентах
     */
    private Double usagePercentage;

    /**
     * Количество временных файлов
     */
    private Long temporaryFiles;

    /**
     * Размер временных файлов в байтах
     */
    private Long temporaryFilesSizeBytes;

    /**
     * Время последней очистки
     */
    private LocalDateTime lastCleanupTime;

    /**
     * Статус доступности хранилища
     */
    private Boolean isAvailable;

    /**
     * Среднее время доступа в миллисекундах
     */
    private Double averageAccessTimeMs;

    /**
     * Количество ошибок за последний час
     */
    private Long errorCountLastHour;

    /**
     * Получить общий размер в мегабайтах
     */
    public Double getTotalSizeMB() {
        return totalSizeBytes != null ? totalSizeBytes / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * Получить доступное место в мегабайтах
     */
    public Double getAvailableSpaceMB() {
        return availableSpaceBytes != null ? availableSpaceBytes / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * Получить размер временных файлов в мегабайтах
     */
    public Double getTemporaryFilesSizeMB() {
        return temporaryFilesSizeBytes != null ? temporaryFilesSizeBytes / (1024.0 * 1024.0) : 0.0;
    }

    /**
     * Проверить, требуется ли очистка
     */
    public boolean needsCleanup() {
        return (temporaryFiles != null && temporaryFiles > 100) ||
               (temporaryFilesSizeBytes != null && temporaryFilesSizeBytes > 100 * 1024 * 1024); // 100MB
    }

    /**
     * Проверить, критически ли заполнено хранилище
     */
    public boolean isCriticallyFull() {
        return usagePercentage != null && usagePercentage > 90.0;
    }

    /**
     * Проверить, близко ли хранилище к заполнению
     */
    public boolean isNearlyFull() {
        return usagePercentage != null && usagePercentage > 80.0;
    }
}