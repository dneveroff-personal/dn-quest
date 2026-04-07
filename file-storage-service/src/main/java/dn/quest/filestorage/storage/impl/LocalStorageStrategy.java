package dn.quest.filestorage.storage.impl;

import dn.quest.filestorage.storage.StorageStatistics;
import dn.quest.filestorage.storage.StorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Стратегия локального файлового хранилища
 */
@Component
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${file.storage.local.path:./uploads}")
    private String basePath;

    @Value("${file.storage.local.temp-path:./temp}")
    private String tempPath;

    @Value("${file.storage.local.max-size:10GB}")
    private String maxStorageSize;

    private static final Map<String, String> DEFAULT_METADATA = new HashMap<>();

    static {
        DEFAULT_METADATA.put("storage-type", "LOCAL");
        DEFAULT_METADATA.put("created-by", "dn-quest-file-storage");
    }

    @Override
    public String storeFile(MultipartFile file, String path, Map<String, String> metadata) {
        try {
            return storeFile(file.getInputStream(), file.getOriginalFilename(), 
                           file.getContentType(), path, metadata);
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String fileName, String contentType, 
                           String path, Map<String, String> metadata) {
        try {
            Path targetPath = buildPath(path, fileName);
            Files.createDirectories(targetPath.getParent());

            // Сохраняем файл
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Сохраняем метаданные
            Map<String, String> allMetadata = new HashMap<>(DEFAULT_METADATA);
            if (metadata != null) {
                allMetadata.putAll(metadata);
            }
            allMetadata.put("content-type", contentType);
            allMetadata.put("original-filename", fileName);
            allMetadata.put("stored-at", LocalDateTime.now().toString());

            saveMetadata(targetPath, allMetadata);

            log.debug("Файл успешно сохранен: {}", targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            log.error("Ошибка при сохранении файла: {}", fileName, e);
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    @Override
    public InputStream loadFile(String path) {
        try {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Файл не найден: " + path);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("Ошибка при загрузке файла: {}", path, e);
            throw new RuntimeException("Не удалось загрузить файл", e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        try {
            Path filePath = Paths.get(path);
            Path metadataPath = getMetadataPath(filePath);

            boolean deleted = Files.deleteIfExists(filePath);
            Files.deleteIfExists(metadataPath);

            if (deleted) {
                log.debug("Файл успешно удален: {}", path);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Ошибка при удалении файла: {}", path, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }

    @Override
    public long getFileSize(String path) {
        try {
            return Files.size(Paths.get(path));
        } catch (IOException e) {
            log.error("Ошибка при получении размера файла: {}", path, e);
            return 0;
        }
    }

    @Override
    public String generatePresignedUrl(String path, Duration duration) {
        // Для локального хранилища возвращаем прямой путь к файлу
        // В реальном приложении здесь может быть генерация временного токена
        return "/api/files/download?path=" + path;
    }

    @Override
    public String generatePresignedUploadUrl(String path, String contentType, Duration duration) {
        // Для локального хранилища возвращаем URL для загрузки
        return "/api/files/upload?path=" + path;
    }

    @Override
    public boolean copyFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            // Копируем метаданные
            Path sourceMetadata = getMetadataPath(source);
            Path targetMetadata = getMetadataPath(target);
            if (Files.exists(sourceMetadata)) {
                Files.copy(sourceMetadata, targetMetadata, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Файл скопирован: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            log.error("Ошибка при копировании файла: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            // Перемещаем метаданные
            Path sourceMetadata = getMetadataPath(source);
            Path targetMetadata = getMetadataPath(target);
            if (Files.exists(sourceMetadata)) {
                Files.move(sourceMetadata, targetMetadata, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Файл перемещен: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            log.error("Ошибка при перемещении файла: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    @Override
    public Map<String, String> getFileMetadata(String path) {
        try {
            Path metadataPath = getMetadataPath(Paths.get(path));
            if (Files.exists(metadataPath)) {
                return loadMetadata(metadataPath);
            }
            return new HashMap<>();
        } catch (IOException e) {
            log.error("Ошибка при загрузке метаданных: {}", path, e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean updateFileMetadata(String path, Map<String, String> metadata) {
        try {
            Path metadataPath = getMetadataPath(Paths.get(path));
            Map<String, String> existingMetadata = loadMetadata(metadataPath);
            existingMetadata.putAll(metadata);
            saveMetadata(metadataPath, existingMetadata);
            return true;
        } catch (IOException e) {
            log.error("Ошибка при обновлении метаданных: {}", path, e);
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    @Override
    public boolean isStorageAvailable() {
        try {
            Path baseDir = Paths.get(basePath);
            return Files.exists(baseDir) && Files.isWritable(baseDir);
        } catch (Exception e) {
            log.error("Ошибка при проверке доступности хранилища", e);
            return false;
        }
    }

    @Override
    public StorageStatistics getStorageStatistics() {
        try {
            Path baseDir = Paths.get(basePath);
            long totalSize = calculateDirectorySize(baseDir);
            long fileCount = countFiles(baseDir);
            
            FileStore store = Files.getFileStore(baseDir);
            long totalSpace = store.getTotalSpace();
            long usableSpace = store.getUsableSpace();
            double usagePercentage = ((double) (totalSpace - usableSpace) / totalSpace) * 100;

            return StorageStatistics.builder()
                    .storageType(getStorageType())
                    .totalFiles(fileCount)
                    .totalSizeBytes(totalSize)
                    .availableSpaceBytes(usableSpace)
                    .usagePercentage(usagePercentage)
                    .isAvailable(isStorageAvailable())
                    .build();
        } catch (IOException e) {
            log.error("Ошибка при получении статистики хранилища", e);
            return StorageStatistics.builder()
                    .storageType(getStorageType())
                    .isAvailable(false)
                    .build();
        }
    }

    @Override
    public void initializeStorage() {
        try {
            Files.createDirectories(Paths.get(basePath));
            Files.createDirectories(Paths.get(tempPath));
            log.info("Локальное хранилище инициализировано: {}", basePath);
        } catch (IOException e) {
            log.error("Ошибка при инициализации хранилища", e);
            throw new RuntimeException("Не удалось инициализировать хранилище", e);
        }
    }

    @Override
    public void cleanupTempFiles() {
        try {
            Path tempDir = Paths.get(tempPath);
            if (!Files.exists(tempDir)) {
                return;
            }

            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.filter(Files::isRegularFile)
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                             log.debug("Удален временный файл: {}", path);
                         } catch (IOException e) {
                             log.warn("Не удалось удалить временный файл: {}", path, e);
                         }
                     });
            }
        } catch (IOException e) {
            log.error("Ошибка при очистке временных файлов", e);
        }
    }

    private Path buildPath(String path, String fileName) {
        Path baseDir = Paths.get(basePath);
        if (path != null && !path.isEmpty()) {
            baseDir = baseDir.resolve(path);
        }
        return baseDir.resolve(fileName);
    }

    private Path getMetadataPath(Path filePath) {
        return Paths.get(filePath.toString() + ".meta");
    }

    private void saveMetadata(Path metadataPath, Map<String, String> metadata) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(metadataPath)) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        }
    }

    private Map<String, String> loadMetadata(Path metadataPath) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        if (!Files.exists(metadataPath)) {
            return metadata;
        }

        try (BufferedReader reader = Files.newBufferedReader(metadataPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    metadata.put(parts[0], parts[1]);
                }
            }
        }
        return metadata;
    }

    private long calculateDirectorySize(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile)
                       .mapToLong(path -> {
                           try {
                               return Files.size(path);
                           } catch (IOException e) {
                               return 0L;
                           }
                       })
                       .sum();
        }
    }

    private long countFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }
}