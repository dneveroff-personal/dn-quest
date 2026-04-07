package dn.quest.filestorage.service.impl;

import dn.quest.filestorage.client.AuthenticationServiceClient;
import dn.quest.filestorage.dto.*;
import dn.quest.filestorage.entity.FileMetadata;
import dn.quest.filestorage.exception.*;
import dn.quest.filestorage.repository.FileMetadataRepository;
import dn.quest.filestorage.service.FileStorageService;
import dn.quest.filestorage.service.FileValidationService;
import dn.quest.filestorage.storage.StorageStrategy;
import dn.quest.filestorage.storage.StorageStrategyFactory;
import dn.quest.filestorage.storage.impl.LocalStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация сервиса для работы с файлами
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileStorageServiceImpl implements FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileValidationService fileValidationService;
    private final StorageStrategyFactory storageStrategyFactory;
    private final AuthenticationServiceClient authServiceClient;
    private final FileStorageServiceImplHelper helper;

    @Override
    public FileResponseDTO uploadFile(MultipartFile file, FileUploadRequestDTO request, String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Начало загрузки файла: {} пользователем: {}", file.getOriginalFilename(), username);

            // Валидация файла
            fileValidationService.validateFile(file, request.getFileType());

            // Получение информации о пользователе
            UUID ownerId = helper.getUserIdByUsername(username);

            // Выбор стратегии хранения
            FileMetadata.StorageType storageType = request.getStorageType() != null 
                    ? request.getStorageType() 
                    : storageStrategyFactory.getOptimalStorageType(request.getFileType());
            
            StorageStrategy strategy = storageStrategyFactory.getStrategy(storageType);
            
            if (!strategy.isStorageAvailable()) {
                log.warn("Основное хранилище недоступно, используется резервное: {}", storageType);
                strategy = storageStrategyFactory.getFallbackStrategy(strategy);
                storageType = strategy instanceof LocalStorageStrategy 
                        ? FileMetadata.StorageType.LOCAL 
                        : FileMetadata.StorageType.MINIO;
            }

            // Генерация имени файла и пути
            String storedFileName = helper.generateStoredFileName(file.getOriginalFilename());
            String path = helper.buildPath(request.getPath(), request.getFileType(), username);

            // Расчет контрольной суммы
            String checksum = helper.calculateChecksum(file);

            // Проверка на дубликаты
            Optional<FileMetadata> existingFile = fileMetadataRepository.findByChecksum(checksum);
            if (existingFile.isPresent() && helper.shouldReuseExistingFile(existingFile.get(), request, ownerId)) {
                log.info("Файл уже существует, используется существующая копия: {}", existingFile.get().getId());
                return helper.convertToResponseDTO(existingFile.get(), strategy);
            }

            // Сохранение файла в хранилище
            Map<String, String> metadata = helper.prepareMetadata(request, username);
            String storagePath = strategy.storeFile(file, path, metadata);

            // Создание метаданных (id генерируется автоматически БД)
            FileMetadata fileMetadata = FileMetadata.builder()
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileType(request.getFileType())
                    .storageType(storageType)
                    .storagePath(storagePath)
                    .description(request.getDescription())
                    .ownerId(ownerId)
                    .isPublic(request.getIsPublic())
                    .isTemporary(request.getIsTemporary())
                    .expiresAt(request.getExpiresAt())
                    .checksum(checksum)
                    .metadataJson(helper.convertMetadataToJson(metadata))
                    .build();

            // Сохранение метаданных в базу
            fileMetadata = fileMetadataRepository.save(fileMetadata);

            log.info("Файл успешно загружен: {} ID: {} за {} мс", 
                    file.getOriginalFilename(), fileMetadata.getId(), 
                    System.currentTimeMillis() - startTime);

            return helper.convertToResponseDTO(fileMetadata, strategy);

        } catch (Exception e) {
            log.error("Ошибка при загрузке файла: {}", file.getOriginalFilename(), e);
            throw new FileUploadException(file.getOriginalFilename(), e.getMessage(), e);
        }
    }

    @Override
    public BatchFileUploadResponseDTO uploadFiles(List<MultipartFile> files, 
                                                 BatchFileUploadRequestDTO request, String username) {
        long startTime = System.currentTimeMillis();
        
        List<FileResponseDTO> uploadedFiles = new ArrayList<>();
        List<BatchFileUploadResponseDTO.FileUploadErrorDTO> errors = new ArrayList<>();
        long totalUploadedSize = 0L;

        for (MultipartFile file : files) {
            try {
                FileUploadRequestDTO fileRequest = FileUploadRequestDTO.builder()
                        .fileType(request.getFileType())
                        .storageType(request.getStorageType())
                        .description(request.getDescription())
                        .path(request.getBasePath())
                        .isPublic(request.getIsPublic())
                        .isTemporary(request.getIsTemporary())
                        .expiresAt(request.getExpiresAt())
                        .metadata(request.getIndividualMetadata() != null 
                                ? request.getIndividualMetadata().get(file.getOriginalFilename())
                                : request.getCommonMetadata())
                        .build();

                FileResponseDTO response = uploadFile(file, fileRequest, username);
                uploadedFiles.add(response);
                totalUploadedSize += response.getFileSize();

            } catch (Exception e) {
                log.error("Ошибка при загрузке файла: {}", file.getOriginalFilename(), e);
                errors.add(BatchFileUploadResponseDTO.FileUploadErrorDTO.builder()
                        .fileName(file.getOriginalFilename())
                        .errorCode("UPLOAD_ERROR")
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        return BatchFileUploadResponseDTO.builder()
                .totalFiles(files.size())
                .successfulUploads(uploadedFiles.size())
                .failedUploads(errors.size())
                .uploadedFiles(uploadedFiles)
                .errors(errors)
                .totalUploadedSize(totalUploadedSize)
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream downloadFile(UUID fileId, String username) {
        try {
            FileMetadata metadata = helper.getFileMetadataById(fileId);
            
            // Проверка прав доступа
            if (!helper.hasFileAccess(metadata, username)) {
                throw new FileAccessException(fileId, "Доступ к файлу запрещен");
            }

            StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
            InputStream inputStream = strategy.loadFile(metadata.getStoragePath());

            // Обновление статистики
            fileMetadataRepository.incrementDownloadCount(fileId, LocalDateTime.now());

            log.info("Файл скачан: {} пользователем: {}", fileId, username);
            return inputStream;

        } catch (Exception e) {
            log.error("Ошибка при скачивании файла: {}", fileId, e);
            throw new FileStorageException("DOWNLOAD_ERROR", "Не удалось скачать файл: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream downloadFileByName(String fileName, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByStoredFileName(fileName)
                    .orElseThrow(() -> new FileNotFoundException(fileName));

            return downloadFile(metadata.getId(), username);

        } catch (Exception e) {
            log.error("Ошибка при скачивании файла по имени: {}", fileName, e);
            throw new FileStorageException("DOWNLOAD_ERROR", "Не удалось скачать файл: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponseDTO getFileMetadata(UUID fileId, String username) {
        try {
            FileMetadata metadata = helper.getFileMetadataById(fileId);
            
            if (!helper.hasFileAccess(metadata, username)) {
                throw new FileAccessException(fileId, "Доступ к метаданным файла запрещен");
            }

            StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
            return helper.convertToResponseDTO(metadata, strategy);

        } catch (Exception e) {
            log.error("Ошибка при получении метаданных файла: {}", fileId, e);
            throw new FileStorageException("METADATA_ERROR", "Не удалось получить метаданные файла: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponseDTO getFileMetadataByName(String fileName, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByStoredFileName(fileName)
                    .orElseThrow(() -> new FileNotFoundException(fileName));

            return getFileMetadata(metadata.getId(), username);

        } catch (Exception e) {
            log.error("Ошибка при получении метаданных файла по имени: {}", fileName, e);
            throw new FileStorageException("METADATA_ERROR", "Не удалось получить метаданные файла: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileSearchResponseDTO searchFiles(FileSearchRequestDTO request, String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Построение спецификации поиска
            Specification<FileMetadata> spec = helper.buildSearchSpecification(request, username);
            
            // Построение сортировки
            Sort sort = Sort.by(
                    request.getSortDirection().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                    request.getSortBy()
            );
            
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // Выполнение поиска
            Page<FileMetadata> page = fileMetadataRepository.findAll(spec, pageable);
            
            // Конвертация результатов
            List<FileResponseDTO> files = page.getContent().stream()
                    .map(metadata -> {
                        StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
                        return helper.convertToResponseDTO(metadata, strategy);
                    })
                    .collect(Collectors.toList());

            // Расчет общего размера
            Long totalSize = page.getContent().stream()
                    .mapToLong(FileMetadata::getFileSize)
                    .sum();

            return FileSearchResponseDTO.builder()
                    .files(files)
                    .currentPage(page.getNumber())
                    .pageSize(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .isFirst(page.isFirst())
                    .isLast(page.isLast())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .totalSizeBytes(totalSize)
                    .formattedTotalSize(FileSearchResponseDTO.formatTotalSize(totalSize))
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при поиске файлов", e);
            throw new FileStorageException("SEARCH_ERROR", "Ошибка при поиске файлов: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteAllUserFiles(UUID userId) {
        log.info("Удаление всех файлов пользователя: {}", userId);
        List<FileMetadata> files = fileMetadataRepository.findAllByOwnerId(userId);
        
        // Удаляем файлы из хранилища
        for (FileMetadata file : files) {
            try {
                StorageStrategy strategy = storageStrategyFactory.getStrategy(file.getStorageType());
                strategy.deleteFile(file.getStoragePath());
            } catch (Exception e) {
                log.warn("Не удалось удалить файл из хранилища: {}", file.getId(), e);
            }
        }
        
        // Массовое удаление из БД
        fileMetadataRepository.deleteAllByOwnerId(userId);
        log.info("Удалено {} файлов пользователя: {}", files.size(), userId);
    }

    @Override
    @Transactional
    public void deleteAllQuestFiles(UUID questId) {
        log.info("Удаление всех файлов квеста: {}", questId);
        List<FileMetadata> files = fileMetadataRepository.findByQuestId(questId);
        
        // Удаляем файлы из хранилища
        for (FileMetadata file : files) {
            try {
                StorageStrategy strategy = storageStrategyFactory.getStrategy(file.getStorageType());
                strategy.deleteFile(file.getStoragePath());
            } catch (Exception e) {
                log.warn("Не удалось удалить файл из хранилища: {}", file.getId(), e);
            }
        }
        
        // Массовое удаление из БД
        fileMetadataRepository.deleteAllByQuestId(questId);
        log.info("Удалено {} файлов квеста: {}", files.size(), questId);
    }

    @Override
    @Transactional
    public void deleteAllTeamFiles(UUID teamId) {
        log.info("Удаление всех файлов команды: {}", teamId);
        List<FileMetadata> files = fileMetadataRepository.findByTeamId(teamId);
        
        // Удаляем файлы из хранилища
        for (FileMetadata file : files) {
            try {
                StorageStrategy strategy = storageStrategyFactory.getStrategy(file.getStorageType());
                strategy.deleteFile(file.getStoragePath());
            } catch (Exception e) {
                log.warn("Не удалось удалить файл из хранилища: {}", file.getId(), e);
            }
        }
        
        // Массовое удаление из БД
        fileMetadataRepository.deleteAllByTeamId(teamId);
        log.info("Удалено {} файлов команды: {}", files.size(), teamId);
    }

    @Override
    public void cleanupExpiredTemporaryFiles() {
        log.info("Очистка истекших временных файлов");
        try {
            List<FileMetadata> expiredFiles = fileMetadataRepository.findExpiredTemporaryFiles(LocalDateTime.now());
            
            // Удаляем файлы из хранилища
            for (FileMetadata file : expiredFiles) {
                try {
                    StorageStrategy strategy = storageStrategyFactory.getStrategy(file.getStorageType());
                    strategy.deleteFile(file.getStoragePath());
                } catch (Exception e) {
                    log.warn("Не удалось удалить файл из хранилища: {}", file.getId(), e);
                }
            }
            
            // Массовое удаление из БД
            fileMetadataRepository.deleteExpiredTemporaryFiles(LocalDateTime.now());
            log.info("Очищено {} истекших временных файлов", expiredFiles.size());
        } catch (Exception e) {
            log.error("Ошибка при очистке истекших временных файлов", e);
            throw new FileStorageException("CLEANUP_ERROR", "Не удалось очистить временные файлы: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasFileAccess(UUID fileId, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId.toString()));
            return helper.hasFileAccess(metadata, username);
        } catch (Exception e) {
            log.error("Ошибка при проверке доступа к файлу: {}", fileId, e);
            return false;
        }
    }

    @Override
    public boolean isFilePublic(UUID fileId) {
        return fileMetadataRepository.findById(fileId)
                .map(FileMetadata::getIsPublic)
                .orElse(false);
    }

    @Override
    public void deleteFile(UUID fileId, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId.toString()));
            
            if (!helper.hasFileAccess(metadata, username)) {
                throw new FileAccessException(fileId, "Доступ к файлу запрещен");
            }
            
            StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
            strategy.deleteFile(metadata.getStoragePath());
            fileMetadataRepository.delete(metadata);
            
            log.info("Файл удален: {} пользователем: {}", fileId, username);
        } catch (Exception e) {
            log.error("Ошибка при удалении файла: {}", fileId, e);
            throw new FileStorageException("DELETE_ERROR", "Не удалось удалить файл: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFileByName(String fileName, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByStoredFileName(fileName)
                    .orElseThrow(() -> new FileNotFoundException(fileName));
            
            deleteFile(metadata.getId(), username);
        } catch (Exception e) {
            log.error("Ошибка при удалении файла по имени: {}", fileName, e);
            throw new FileStorageException("DELETE_ERROR", "Не удалось удалить файл: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedDownloadUrl(UUID fileId, Duration duration, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId.toString()));
            
            if (!helper.hasFileAccess(metadata, username)) {
                throw new FileAccessException(fileId, "Доступ к файлу запрещен");
            }
            
            StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
            return strategy.generatePresignedUrl(metadata.getStoragePath(), duration);
        } catch (Exception e) {
            log.error("Ошибка при генерации предписанного URL для скачивания: {}", fileId, e);
            throw new FileStorageException("PRESIGNED_URL_ERROR", "Не удалось сгенерировать URL: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUploadUrl(String fileName, String contentType, Duration duration, String username) {
        try {
            UUID ownerId = helper.getUserIdByUsername(username);
            
            String storedFileName = helper.generateStoredFileName(fileName);
            String path = helper.buildPath(null, FileMetadata.FileType.OTHER, username);
            String fullPath = path + storedFileName;
            
            FileMetadata.StorageType storageType = storageStrategyFactory.getOptimalStorageType(FileMetadata.FileType.OTHER);
            StorageStrategy strategy = storageStrategyFactory.getStrategy(storageType);
            
            return strategy.generatePresignedUploadUrl(fullPath, contentType, duration);
        } catch (Exception e) {
            log.error("Ошибка при генерации предписанного URL для загрузки: {}", fileName, e);
            throw new FileStorageException("PRESIGNED_UPLOAD_URL_ERROR", "Не удалось сгенерировать URL для загрузки: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<FileResponseDTO> getTemporaryFiles(Pageable pageable, String username) {
        try {
            Page<FileMetadata> page = fileMetadataRepository.findByIsTemporaryTrue(pageable);
            List<FileResponseDTO> files = page.getContent().stream()
                    .map(metadata -> {
                        StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
                        return helper.convertToResponseDTO(metadata, strategy);
                    })
                    .collect(Collectors.toList());
            
            return new PageImpl<>(files, pageable, page.getTotalElements());
        } catch (Exception e) {
            log.error("Ошибка при получении временных файлов", e);
            throw new FileStorageException("TEMPORARY_FILES_ERROR", "Не удалось получить временные файлы: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<FileResponseDTO> getPublicFiles(Pageable pageable) {
        try {
            Page<FileMetadata> page = fileMetadataRepository.findByIsPublicTrue(pageable);
            List<FileResponseDTO> files = page.getContent().stream()
                    .map(metadata -> {
                        StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
                        return helper.convertToResponseDTO(metadata, strategy);
                    })
                    .collect(Collectors.toList());
            
            return new PageImpl<>(files, pageable, page.getTotalElements());
        } catch (Exception e) {
            log.error("Ошибка при получении публичных файлов", e);
            throw new FileStorageException("PUBLIC_FILES_ERROR", "Не удалось получить публичные файлы: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<FileResponseDTO> getFilesByOwner(UUID ownerId, Pageable pageable, String username) {
        try {
            Page<FileMetadata> page = fileMetadataRepository.findByOwnerId(ownerId, pageable);
            List<FileResponseDTO> files = page.getContent().stream()
                    .map(metadata -> {
                        StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
                        return helper.convertToResponseDTO(metadata, strategy);
                    })
                    .collect(Collectors.toList());
            
            return new PageImpl<>(files, pageable, page.getTotalElements());
        } catch (Exception e) {
            log.error("Ошибка при получении файлов владельца", e);
            throw new FileStorageException("OWNER_FILES_ERROR", "Не удалось получить файлы: " + e.getMessage(), e);
        }
    }

    @Override
    public FileResponseDTO updateFileMetadata(UUID fileId, FileUploadRequestDTO request, String username) {
        try {
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new FileNotFoundException(fileId.toString()));
            
            if (!helper.hasFileAccess(metadata, username)) {
                throw new FileAccessException(fileId, "Доступ к файлу запрещен");
            }
            
            metadata.setDescription(request.getDescription());
            metadata.setIsPublic(request.getIsPublic());
            metadata.setIsTemporary(request.getIsTemporary());
            metadata.setExpiresAt(request.getExpiresAt());
            
            metadata = fileMetadataRepository.save(metadata);
            
            StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
            return helper.convertToResponseDTO(metadata, strategy);
        } catch (Exception e) {
            log.error("Ошибка при обновлении метаданных файла: {}", fileId, e);
            throw new FileStorageException("UPDATE_METADATA_ERROR", "Не удалось обновить метаданные: " + e.getMessage(), e);
        }
    }

    @Override
    public FileResponseDTO copyFile(UUID sourceFileId, FileUploadRequestDTO request, String username) {
        try {
            FileMetadata sourceMetadata = fileMetadataRepository.findById(sourceFileId)
                    .orElseThrow(() -> new FileNotFoundException(sourceFileId.toString()));
            
            if (!helper.hasFileAccess(sourceMetadata, username)) {
                throw new FileAccessException(sourceFileId, "Доступ к файлу запрещен");
            }
            
            // Получаем ID владельца
            UUID ownerId = helper.getUserIdByUsername(username);
            
            // Определяем тип хранилища
            FileMetadata.StorageType targetStorageType = request.getStorageType() != null 
                    ? request.getStorageType() 
                    : sourceMetadata.getStorageType();
            
            StorageStrategy sourceStrategy = storageStrategyFactory.getStrategy(sourceMetadata.getStorageType());
            StorageStrategy targetStrategy = storageStrategyFactory.getStrategy(targetStorageType);
            
            // Загружаем файл из источника
            InputStream inputStream = sourceStrategy.loadFile(sourceMetadata.getStoragePath());
            
            // Генерируем новое имя и путь
            String newStoredFileName = helper.generateStoredFileName(sourceMetadata.getOriginalFileName());
            String newPath = helper.buildPath(request.getPath(), sourceMetadata.getFileType(), username);
            
            // Сохраняем файл в целевом хранилище
            String storagePath = targetStrategy.storeFile(inputStream, newStoredFileName, 
                    sourceMetadata.getContentType(), newPath, new HashMap<>());
            
            // Создаем метаданные для нового файла
            FileMetadata newMetadata = FileMetadata.builder()
                    .originalFileName(sourceMetadata.getOriginalFileName())
                    .storedFileName(newStoredFileName)
                    .contentType(sourceMetadata.getContentType())
                    .fileSize(sourceMetadata.getFileSize())
                    .fileType(sourceMetadata.getFileType())
                    .storageType(targetStorageType)
                    .storagePath(storagePath)
                    .description(request.getDescription())
                    .ownerId(ownerId)
                    .isPublic(request.getIsPublic())
                    .isTemporary(request.getIsTemporary())
                    .expiresAt(request.getExpiresAt())
                    .checksum(sourceMetadata.getChecksum())
                    .metadataJson(sourceMetadata.getMetadataJson())
                    .build();
            
            newMetadata = fileMetadataRepository.save(newMetadata);
            return helper.convertToResponseDTO(newMetadata, targetStrategy);
        } catch (Exception e) {
            log.error("Ошибка при копировании файла: {}", sourceFileId, e);
            throw new FileStorageException("COPY_FILE_ERROR", "Не удалось скопировать файл: " + e.getMessage(), e);
        }
    }

    @Override
    public FileResponseDTO moveFile(UUID sourceFileId, FileUploadRequestDTO request, String username) {
        try {
            FileMetadata sourceMetadata = fileMetadataRepository.findById(sourceFileId)
                    .orElseThrow(() -> new FileNotFoundException(sourceFileId.toString()));
            
            if (!helper.hasFileAccess(sourceMetadata, username)) {
                throw new FileAccessException(sourceFileId, "Доступ к файлу запрещен");
            }
            
            // Определяем тип хранилища
            FileMetadata.StorageType targetStorageType = request.getStorageType() != null 
                    ? request.getStorageType() 
                    : sourceMetadata.getStorageType();
            
            StorageStrategy sourceStrategy = storageStrategyFactory.getStrategy(sourceMetadata.getStorageType());
            StorageStrategy targetStrategy = storageStrategyFactory.getStrategy(targetStorageType);
            
            String newStoragePath;
            
            // Если тот же тип хранилища, используем moveFile стратегии
            if (sourceMetadata.getStorageType() == targetStorageType) {
                String newStoredFileName = helper.generateStoredFileName(sourceMetadata.getOriginalFileName());
                String newPath = helper.buildPath(request.getPath(), sourceMetadata.getFileType(), username);
                newStoragePath = newPath + newStoredFileName;
                
                sourceStrategy.moveFile(sourceMetadata.getStoragePath(), newStoragePath);
            } else {
                // Разные хранилища - копируем и удаляем
                InputStream inputStream = sourceStrategy.loadFile(sourceMetadata.getStoragePath());
                String newStoredFileName = helper.generateStoredFileName(sourceMetadata.getOriginalFileName());
                String newPath = helper.buildPath(request.getPath(), sourceMetadata.getFileType(), username);
                newStoragePath = targetStrategy.storeFile(inputStream, newStoredFileName, 
                        sourceMetadata.getContentType(), newPath, new HashMap<>());
                sourceStrategy.deleteFile(sourceMetadata.getStoragePath());
            }
            
            // Обновляем метаданные
            sourceMetadata.setStoragePath(newStoragePath);
            sourceMetadata.setStorageType(targetStorageType);
            sourceMetadata.setDescription(request.getDescription());
            sourceMetadata.setIsPublic(request.getIsPublic());
            sourceMetadata.setIsTemporary(request.getIsTemporary());
            sourceMetadata.setExpiresAt(request.getExpiresAt());
            
            sourceMetadata = fileMetadataRepository.save(sourceMetadata);
            return helper.convertToResponseDTO(sourceMetadata, targetStrategy);
        } catch (Exception e) {
            log.error("Ошибка при перемещении файла: {}", sourceFileId, e);
            throw new FileStorageException("MOVE_FILE_ERROR", "Не удалось переместить файл: " + e.getMessage(), e);
        }
    }

    @Override
    public UserStorageStatisticsDTO getUserStorageStatistics(UUID userId, String username) {
        log.info("Получение статистики хранилища для пользователя: {}", userId);
        
        try {
            // Используем стриминг для избежания проблем с памятью при большом количестве файлов
            try (Stream<FileMetadata> fileStream = fileMetadataRepository.streamByOwnerId(userId)) {
                Map<String, Long> filesByType = fileStream
                        .collect(Collectors.groupingBy(f -> f.getFileType().name(), Collectors.counting()));
                
                long totalFiles = filesByType.values().stream().mapToLong(Long::longValue).sum();
                
                // Повторный стрим для подсчета общего размера
                try (Stream<FileMetadata> sizeStream = fileMetadataRepository.streamByOwnerId(userId)) {
                    long totalSize = sizeStream.mapToLong(FileMetadata::getFileSize).sum();
                    
                    return UserStorageStatisticsDTO.builder()
                            .userId(userId)
                            .totalFiles(totalFiles)
                            .totalSizeBytes(totalSize)
                            .formattedTotalSize(UserStorageStatisticsDTO.formatSize(totalSize))
                            .filesByType(filesByType)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при получении статистики хранилища для пользователя: {}", userId, e);
            throw new FileStorageException("STATISTICS_ERROR", "Не удалось получить статистику: " + e.getMessage(), e);
        }
    }

    @Override
    public StorageStatisticsDTO getStorageStatistics() {
        log.info("Получение общей статистики хранилища");
        
        try {
            Long totalSize = fileMetadataRepository.getTotalStorageSize();
            Long totalFiles = fileMetadataRepository.getTotalFileCount();
            
            return StorageStatisticsDTO.builder()
                    .totalFiles(totalFiles != null ? totalFiles : 0L)
                    .totalSizeBytes(totalSize != null ? totalSize : 0L)
                    .formattedTotalSize(StorageStatisticsDTO.formatSize(totalSize != null ? totalSize : 0L))
                    .build();
        } catch (Exception e) {
            log.error("Ошибка при получении статистики хранилища", e);
            throw new FileStorageException("STATISTICS_ERROR", "Не удалось получить статистику: " + e.getMessage(), e);
        }
    }
}