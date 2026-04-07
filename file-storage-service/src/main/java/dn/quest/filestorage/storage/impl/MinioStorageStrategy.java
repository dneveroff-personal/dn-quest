package dn.quest.filestorage.storage.impl;

import dn.quest.filestorage.storage.StorageStatistics;
import dn.quest.filestorage.storage.StorageStrategy;
import io.minio.*;
import io.minio.messages.Item;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Стратегия MinIO хранилища
 */
@Component
@Slf4j
public class MinioStorageStrategy implements StorageStrategy {

    private MinioClient minioClient;
    private final String bucketName;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${minio.region:us-east-1}")
    private String region;

    private static final Map<String, String> DEFAULT_METADATA = new HashMap<>();

    static {
        DEFAULT_METADATA.put("storage-type", "MINIO");
        DEFAULT_METADATA.put("created-by", "dn-quest-file-storage");
    }

    public MinioStorageStrategy() {
        // Временная инициализация, будет заменена через @PostConstruct
        this.minioClient = null;
        this.bucketName = "dn-quest-files";
    }

    /**
     * Инициализация MinIO клиента
     */
    private void initializeClient() {
        if (minioClient == null) {
            synchronized (this) {
                if (minioClient == null) {
                    this.minioClient = MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .region(region)
                            .build();
                }
            }
        }
    }

    @Override
    public String storeFile(MultipartFile file, String path, Map<String, String> metadata) {
        try {
            return storeFile(file.getInputStream(), file.getOriginalFilename(), 
                           file.getContentType(), path, metadata);
        } catch (Exception e) {
            log.error("Ошибка при сохранении файла в MinIO: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    @Override
    public String storeFile(InputStream inputStream, String fileName, String contentType, 
                           String path, Map<String, String> metadata) {
        initializeClient();
        
        try {
            String objectName = buildObjectName(path, fileName);
            
            // Подготавливаем метаданные
            Map<String, String> allMetadata = new HashMap<>(DEFAULT_METADATA);
            if (metadata != null) {
                allMetadata.putAll(metadata);
            }
            allMetadata.put("content-type", contentType);
            allMetadata.put("original-filename", fileName);
            allMetadata.put("stored-at", LocalDateTime.now().toString());

            // Загружаем файл
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760) // 10MB part size
                            .contentType(contentType)
                            .userMetadata(allMetadata)
                            .build()
            );

            log.debug("Файл успешно сохранен в MinIO: {}", objectName);
            return objectName;

        } catch (Exception e) {
            log.error("Ошибка при сохранении файла в MinIO: {}", fileName, e);
            throw new RuntimeException("Не удалось сохранить файл", e);
        }
    }

    @Override
    public InputStream loadFile(String path) {
        initializeClient();
        
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла из MinIO: {}", path, e);
            throw new RuntimeException("Не удалось загрузить файл", e);
        }
    }

    @Override
    public boolean deleteFile(String path) {
        initializeClient();
        
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );

            log.debug("Файл успешно удален из MinIO: {}", path);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении файла из MinIO: {}", path, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String path) {
        initializeClient();
        
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getFileSize(String path) {
        initializeClient();
        
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return stat.size();
        } catch (Exception e) {
            log.error("Ошибка при получении размера файла из MinIO: {}", path, e);
            return 0;
        }
    }

    @Override
    public String generatePresignedUrl(String path, Duration duration) {
        initializeClient();
        
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(path)
                            .expiry((int) duration.getSeconds())
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при генерации предписанного URL для скачивания: {}", path, e);
            throw new RuntimeException("Не удалось сгенерировать URL", e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String path, String contentType, Duration duration) {
        initializeClient();
        
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(path)
                            .expiry((int) duration.getSeconds())
                            .extraQueryParams(headers)
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка при генерации предписанного URL для загрузки: {}", path, e);
            throw new RuntimeException("Не удалось сгенерировать URL", e);
        }
    }

    @Override
    public boolean copyFile(String sourcePath, String targetPath) {
        initializeClient();
        
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetPath)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(sourcePath)
                                    .build())
                            .build()
            );

            log.debug("Файл скопирован в MinIO: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при копировании файла в MinIO: {} -> {}", sourcePath, targetPath, e);
            return false;
        }
    }

    @Override
    public boolean moveFile(String sourcePath, String targetPath) {
        if (copyFile(sourcePath, targetPath)) {
            return deleteFile(sourcePath);
        }
        return false;
    }

    @Override
    public Map<String, String> getFileMetadata(String path) {
        initializeClient();
        
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return stat.userMetadata();
        } catch (Exception e) {
            log.error("Ошибка при получении метаданных из MinIO: {}", path, e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean updateFileMetadata(String path, Map<String, String> metadata) {
        // MinIO не поддерживает прямое обновление метаданных
        // Нужно скопировать файл с новыми метаданными
        String tempPath = path + ".temp";
        if (copyFile(path, tempPath)) {
            try {
                // Добавляем новые метаданные к существующим
                Map<String, String> existingMetadata = getFileMetadata(path);
                existingMetadata.putAll(metadata);

                // Удаляем старый файл
                deleteFile(path);
                
                // Переименовываем временный файл
                return moveFile(tempPath, path);
            } catch (Exception e) {
                log.error("Ошибка при обновлении метаданных в MinIO: {}", path, e);
                deleteFile(tempPath); // Очищаем временный файл
                return false;
            }
        }
        return false;
    }

    @Override
    public String getStorageType() {
        return "MINIO";
    }

    @Override
    public boolean isStorageAvailable() {
        initializeClient();
        
        try {
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            log.error("MinIO хранилище недоступно", e);
            return false;
        }
    }

    @Override
    public StorageStatistics getStorageStatistics() {
        initializeClient();
        
        try {
            long totalSize = 0L;
            long fileCount = 0L;

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                totalSize += item.size();
                fileCount++;
            }

            return StorageStatistics.builder()
                    .storageType(getStorageType())
                    .totalFiles(fileCount)
                    .totalSizeBytes(totalSize)
                    .isAvailable(isStorageAvailable())
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при получении статистики MinIO", e);
            return StorageStatistics.builder()
                    .storageType(getStorageType())
                    .isAvailable(false)
                    .build();
        }
    }

    @Override
    public void initializeStorage() {
        initializeClient();
        
        try {
            // Проверяем существование бакета
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                // Создаем бакет
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .region(region)
                                .build()
                );
                log.info("Создан новый бакет MinIO: {}", bucketName);
            }

            log.info("MinIO хранилище инициализировано: {}", endpoint);
        } catch (Exception e) {
            log.error("Ошибка при инициализации MinIO хранилища", e);
            throw new RuntimeException("Не удалось инициализировать хранилище", e);
        }
    }

    @Override
    public void cleanupTempFiles() {
        initializeClient();
        
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix("temp/")
                            .build()
            );

            int deletedCount = 0;
            for (Result<Item> result : results) {
                Item item = result.get();
                if (deleteFile(item.objectName())) {
                    deletedCount++;
                }
            }

            log.info("Удалено временных файлов из MinIO: {}", deletedCount);
        } catch (Exception e) {
            log.error("Ошибка при очистке временных файлов в MinIO", e);
        }
    }

    private String buildObjectName(String path, String fileName) {
        if (path != null && !path.isEmpty() && !path.endsWith("/")) {
            return path + "/" + fileName;
        } else if (path != null && !path.isEmpty()) {
            return path + fileName;
        }
        return fileName;
    }
}