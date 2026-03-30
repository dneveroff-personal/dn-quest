package dn.quest.filestorage.service.impl;

import dn.quest.filestorage.client.AuthenticationServiceClient;
import dn.quest.filestorage.dto.*;
import dn.quest.filestorage.entity.FileMetadata;
import dn.quest.filestorage.exception.*;
import dn.quest.filestorage.repository.FileMetadataRepository;
import dn.quest.filestorage.storage.StorageStrategy;
import dn.quest.shared.dto.UserDTO;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.criteria.Predicate;

/**
 * Вспомогательные методы для FileStorageServiceImpl
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImplHelper {

    private final FileMetadataRepository fileMetadataRepository;
    private final AuthenticationServiceClient authServiceClient;

    /**
     * Получить ID пользователя по имени
     */
    protected Long getUserIdByUsername(String username) {
        try {
            UserDTO user = authServiceClient.getUserByUsername(username);
            return user.getId();
        } catch (Exception e) {
            log.error("Ошибка при получении ID пользователя: {}", username, e);
            throw new FileStorageException("USER_NOT_FOUND", "Пользователь не найден: " + username);
        }
    }

    /**
     * Сгенерировать имя для хранения файла
     */
    protected String generateStoredFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        
        return UUID.randomUUID() + extension;
    }

    /**
     * Построить путь для хранения файла
     */
    protected String buildPath(String customPath, FileMetadata.FileType fileType, String username) {
        StringBuilder path = new StringBuilder();
        
        // Добавляем базовый путь
        if (customPath != null && !customPath.trim().isEmpty()) {
            path.append(customPath.trim());
            if (!customPath.endsWith("/")) {
                path.append("/");
            }
        } else {
            // Путь по умолчанию для типа файла
            path.append(fileType.name().toLowerCase()).append("/");
        }
        
        // Добавляем путь пользователя
        path.append(username).append("/");
        
        // Добавляем путь по дате
        LocalDateTime now = LocalDateTime.now();
        path.append(String.format("%d/%02d/%02d/", now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
        
        return path.toString();
    }

    /**
     * Рассчитать контрольную сумму файла
     */
    protected String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.IOException e) {
            log.error("Ошибка при расчете контрольной суммы", e);
            return null;
        }
    }

    /**
     * Проверить, можно ли переиспользовать существующий файл
     */
    protected boolean shouldReuseExistingFile(FileMetadata existingFile, FileUploadRequestDTO request, Long ownerId) {
        return existingFile.getOwnerId().equals(ownerId) &&
               existingFile.getFileType().equals(request.getFileType()) &&
               Objects.equals(existingFile.getIsPublic(), request.getIsPublic()) &&
               Objects.equals(existingFile.getIsTemporary(), request.getIsTemporary());
    }

    /**
     * Подготовить метаданные для хранения
     */
    protected Map<String, String> prepareMetadata(FileUploadRequestDTO request, String username) {
        Map<String, String> metadata = new HashMap<>();
        
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }
        
        metadata.put("uploaded-by", username);
        metadata.put("upload-timestamp", LocalDateTime.now().toString());
        metadata.put("file-type", request.getFileType().name());
        
        if (request.getDescription() != null) {
            metadata.put("description", request.getDescription());
        }
        
        return metadata;
    }

    /**
     * Конвертировать метаданные в JSON
     */
    protected String convertMetadataToJson(Map<String, String> metadata) {
        try {
            if (metadata == null || metadata.isEmpty()) {
                return null;
            }
            
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
            
            return json.toString();
        } catch (Exception e) {
            log.error("Ошибка при конвертации метаданных в JSON", e);
            return null;
        }
    }

    /**
     * Конвертировать метаданные из JSON
     */
    protected Map<String, String> convertMetadataFromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new HashMap<>();
            }
            
            Map<String, String> metadata = new HashMap<>();
            // Простая парсилка JSON (для production лучше использовать Jackson)
            String content = json.trim().substring(1, json.length() - 1); // Убираем { }
            String[] pairs = content.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split("\":\"");
                if (keyValue.length == 2) {
                    String key = keyValue[0].substring(1); // Убираем "
                    String value = keyValue[1].substring(0, keyValue[1].length() - 1); // Убираем "
                    metadata.put(key, value);
                }
            }
            
            return metadata;
        } catch (Exception e) {
            log.error("Ошибка при конвертации метаданных из JSON", e);
            return new HashMap<>();
        }
    }

    /**
     * Получить метаданные файла по ID
     */
    protected FileMetadata getFileMetadataById(UUID fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    /**
     * Проверить права доступа к файлу
     */
    protected boolean hasFileAccess(FileMetadata metadata, String username) {
        // Публичные файлы доступны всем
        if (Boolean.TRUE.equals(metadata.getIsPublic())) {
            return true;
        }
        
        // Владелец имеет доступ
        try {
            Long userId = getUserIdByUsername(username);
            return metadata.getOwnerId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Конвертировать сущность в DTO
     */
    protected FileResponseDTO convertToResponseDTO(FileMetadata metadata, StorageStrategy strategy) {
        Map<String, String> additionalMetadata = convertMetadataFromJson(metadata.getMetadataJson());
        
        return FileResponseDTO.builder()
                .id(metadata.getId())
                .originalFileName(metadata.getOriginalFileName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .formattedFileSize(FileResponseDTO.formatFileSize(metadata.getFileSize()))
                .fileType(metadata.getFileType())
                .storageType(metadata.getStorageType())
                .description(metadata.getDescription())
                .ownerId(metadata.getOwnerId())
                .isPublic(metadata.getIsPublic())
                .isTemporary(metadata.getIsTemporary())
                .expiresAt(metadata.getExpiresAt())
                .downloadCount(metadata.getDownloadCount())
                .lastAccessedAt(metadata.getLastAccessedAt())
                .checksum(metadata.getChecksum())
                .thumbnailPath(metadata.getThumbnailPath())
                .metadata(additionalMetadata)
                .createdAt(metadata.getCreatedAt())
                .updatedAt(metadata.getUpdatedAt())
                .downloadUrl(strategy.generatePresignedUrl(metadata.getStoragePath(), Duration.ofHours(1)))
                .status(FileResponseDTO.determineStatus(metadata))
                .timeToExpiration(FileResponseDTO.getTimeToExpiration(metadata.getExpiresAt()))
                .build();
    }

    /**
     * Построить спецификацию поиска
     */
    protected Specification<FileMetadata> buildSearchSpecification(FileSearchRequestDTO request, String username) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Фильтр по владельцу (если не администратор)
            try {
                Long userId = getUserIdByUsername(username);
                predicates.add(criteriaBuilder.equal(root.get("ownerId"), userId));
            } catch (Exception e) {
                // Если не удалось получить ID пользователя, возвращаем пустой результат
                return criteriaBuilder.disjunction();
            }
            
            // Фильтр по типу файла
            if (request.getFileType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("fileType"), request.getFileType()));
            }
            
            // Фильтр по типу хранилища
            if (request.getStorageType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("storageType"), request.getStorageType()));
            }
            
            // Фильтр по контент типу
            if (request.getContentType() != null) {
                predicates.add(criteriaBuilder.like(root.get("contentType"), "%" + request.getContentType() + "%"));
            }
            
            // Фильтр по имени файла
            if (request.getFileName() != null) {
                predicates.add(criteriaBuilder.like(root.get("originalFileName"), "%" + request.getFileName() + "%"));
            }
            
            // Фильтр по публичности
            if (request.getIsPublic() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), request.getIsPublic()));
            }
            
            // Фильтр по временным файлам
            if (request.getIsTemporary() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isTemporary"), request.getIsTemporary()));
            }
            
            // Фильтр по размеру файла
            if (request.getMinFileSize() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fileSize"), request.getMinFileSize()));
            }
            
            if (request.getMaxFileSize() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fileSize"), request.getMaxFileSize()));
            }
            
            // Фильтр по дате создания
            if (request.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedFrom()));
            }
            
            if (request.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedTo()));
            }
            
            // Фильтр по дате последнего доступа
            if (request.getLastAccessedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastAccessedAt"), request.getLastAccessedFrom()));
            }
            
            if (request.getLastAccessedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("lastAccessedAt"), request.getLastAccessedTo()));
            }
            
            // Фильтр по списку ID
            if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
                predicates.add(root.get("id").in(request.getFileIds()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}