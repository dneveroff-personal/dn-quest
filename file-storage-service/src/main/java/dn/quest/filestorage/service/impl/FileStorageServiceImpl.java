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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public FileResponseDTO uploadFile(MultipartFile file, FileUploadRequestDTO request, String username) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Начало загрузки файла: {} пользователем: {}", file.getOriginalFilename(), username);

            // Валидация файла
            fileValidationService.validateFile(file, request.getFileType());

            // Получение информации о пользователе
            UUID ownerId = getUserIdByUsername(username);

            // Выбор стратегии хранения
            FileMetadata.StorageType storageType = request.getStorageType() != null 
                    ? request.getStorageType() 
                    : storageStrategyFactory.getOptimalStrategy(request.getFileType())
                            .getStorageType();
            
            StorageStrategy strategy = storageStrategyFactory.getStrategy(storageType);
            
            if (!strategy.isStorageAvailable()) {
                log.warn("Основное хранилище недоступно, используется резервное: {}", storageType);
                strategy = storageStrategyFactory.getFallbackStrategy(strategy);
                storageType = strategy.getStorageType();
            }

            // Генерация имени файла и пути
            String storedFileName = generateStoredFileName(file.getOriginalFilename());
            String path = buildPath(request.getPath(), request.getFileType(), username);

            // Расчет контрольной суммы
            String checksum = calculateChecksum(file);

            // Проверка на дубликаты
            Optional<FileMetadata> existingFile = fileMetadataRepository.findByChecksum(checksum);
            if (existingFile.isPresent() && shouldReuseExistingFile(existingFile.get(), request, ownerId)) {
                log.info("Файл уже существует, используется существующая копия: {}", existingFile.get().getId());
                return convertToResponseDTO(existingFile.get(), strategy);
            }

            // Сохранение файла в хранилище
            Map<String, String> metadata = prepareMetadata(request, username);
            String storagePath = strategy.storeFile(file, path, metadata);

            // Создание метаданных
            FileMetadata fileMetadata = FileMetadata.builder()
                    .id(UUID.randomUUID())
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
                    .metadataJson(convertMetadataToJson(metadata))
                    .build();

            // Сохранение метаданных в базу
            fileMetadata = fileMetadataRepository.save(fileMetadata);

            log.info("Файл успешно загружен: {} ID: {} за {} мс", 
                    file.getOriginalFilename(), fileMetadata.getId(), 
                    System.currentTimeMillis() - startTime);

            return convertToResponseDTO(fileMetadata, strategy);

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
            FileMetadata metadata = getFileMetadataById(fileId);
            
            // Проверка прав доступа
            if (!hasFileAccess(metadata, username)) {
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
            FileMetadata metadata = getFileMetadataById(fileId);
            
            if (!hasFileAccess(metadata, username)) {
                throw new FileAccessException(fileId, "Доступ к метаданным файла запрещен");
            }

            StorageStrategy strategy = storageStrategyFactory.getStrategy(metadata.getStorageType());
            return convertToResponseDTO(metadata, strategy);

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
            Specification<FileMetadata> spec = buildSearchSpecification(request, username);
            
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
                        return convertToResponseDTO(metadata, strategy);
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

    // Вспомогательные методы будут в следующей части...
}