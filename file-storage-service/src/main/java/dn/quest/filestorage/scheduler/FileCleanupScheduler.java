package dn.quest.filestorage.scheduler;

import dn.quest.filestorage.entity.FileMetadata;
import dn.quest.filestorage.repository.FileMetadataRepository;
import dn.quest.filestorage.service.FileStorageService;
import dn.quest.filestorage.storage.StorageStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Планировщик для очистки временных файлов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileCleanupScheduler {

    private final FileMetadataRepository fileMetadataRepository;
    private final StorageStrategyFactory storageStrategyFactory;

    /**
     * Очистка истекших временных файлов
     * Выполняется каждый час
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredTemporaryFiles() {
        log.info("Начало очистки истекших временных файлов");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Находим истекшие временные файлы
            var expiredFiles = fileMetadataRepository.findExpiredTemporaryFiles(now);
            
            if (expiredFiles.isEmpty()) {
                log.info("Истекших временных файлов не найдено");
                return;
            }
            
            log.info("Найдено {} истекших временных файлов", expiredFiles.size());
            
            int deletedCount = 0;
            long totalSizeFreed = 0;
            
            for (FileMetadata fileMetadata : expiredFiles) {
                try {
                    // Удаляем файл из хранилища
                    var strategy = storageStrategyFactory.getStrategy(fileMetadata.getStorageType());
                    boolean deleted = strategy.deleteFile(fileMetadata.getStoragePath());
                    
                    if (deleted) {
                        totalSizeFreed += fileMetadata.getFileSize();
                        deletedCount++;
                    }
                    
                    // Удаляем метаданные из базы
                    fileMetadataRepository.delete(fileMetadata);
                    
                    log.debug("Удален истекший временный файл: {} ({} bytes)", 
                            fileMetadata.getOriginalFileName(), fileMetadata.getFileSize());
                    
                } catch (Exception e) {
                    log.error("Ошибка при удалении истекшего временного файла: {}", 
                            fileMetadata.getOriginalFileName(), e);
                }
            }
            
            log.info("Очистка завершена. Удалено файлов: {}, освобождено места: {} bytes", 
                    deletedCount, totalSizeFreed);
            
        } catch (Exception e) {
            log.error("Ошибка при очистке истекших временных файлов", e);
        }
    }

    /**
     * Очистка временных файлов в хранилищах
     * Выполняется каждые 6 часов
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void cleanupStorageTempFiles() {
        log.info("Начало очистки временных файлов в хранилищах");
        
        try {
            storageStrategyFactory.cleanupAllTempFiles();
            log.info("Очистка временных файлов в хранилищах завершена");
            
        } catch (Exception e) {
            log.error("Ошибка при очистке временных файлов в хранилищах", e);
        }
    }

    /**
     * Генерация статистики и отчетов
     * Выполняется каждый день в 3 часа ночи
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void generateDailyStatistics() {
        log.info("Начало генерации ежедневной статистики");
        
        try {
            // Получаем общую статистику
            var totalFiles = fileMetadataRepository.getTotalFileCount();
            var totalSize = fileMetadataRepository.getTotalStorageSize();
            
            log.info("Ежедневная статистика: файлов - {}, общий размер - {} bytes", 
                    totalFiles, totalSize);
            
            // Получаем статистику по типам файлов
            var fileTypeStats = fileMetadataRepository.getFileStatisticsByType();
            log.info("Статистика по типам файлов:");
            for (Object[] stat : fileTypeStats) {
                log.info("  {}: {} файлов, {} bytes", stat[0], stat[1], stat[2]);
            }
            
            // Получаем статистику по хранилищам
            var storageStats = fileMetadataRepository.getFileStatisticsByStorage();
            log.info("Статистика по хранилищам:");
            for (Object[] stat : storageStats) {
                log.info("  {}: {} файлов, {} bytes", stat[0], stat[1], stat[2]);
            }
            
            log.info("Генерация ежедневной статистики завершена");
            
        } catch (Exception e) {
            log.error("Ошибка при генерации ежедневной статистики", e);
        }
    }

    /**
     * Проверка состояния хранилищ
     * Выполняется каждые 30 минут
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkStorageHealth() {
        log.debug("Начало проверки состояния хранилищ");
        
        try {
            var allStats = storageStrategyFactory.getAllStorageStatistics();
            
            for (var entry : allStats.entrySet()) {
                var storageType = entry.getKey();
                var stats = entry.getValue();
                
                if (!stats.getIsAvailable()) {
                    log.warn("Хранилище {} недоступно", storageType);
                }
                
                if (stats.isCriticallyFull()) {
                    log.error("Хранилище {} критически заполнено: {}%", 
                            storageType, stats.getUsagePercentage());
                } else if (stats.isNearlyFull()) {
                    log.warn("Хранилище {} почти заполнено: {}%", 
                            storageType, stats.getUsagePercentage());
                }
                
                if (stats.needsCleanup()) {
                    log.info("Хранилищу {} требуется очистка временных файлов", storageType);
                }
            }
            
            log.debug("Проверка состояния хранилищ завершена");
            
        } catch (Exception e) {
            log.error("Ошибка при проверке состояния хранилищ", e);
        }
    }
}