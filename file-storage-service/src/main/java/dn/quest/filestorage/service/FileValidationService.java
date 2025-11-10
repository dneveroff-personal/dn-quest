package dn.quest.filestorage.service;

import dn.quest.filestorage.entity.FileMetadata;
import dn.quest.filestorage.exception.FileValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Сервис для валидации файлов
 */
@Service
public class FileValidationService {

    @Value("${file.upload.max-size:50MB}")
    private String maxFileSize;

    @Value("${file.upload.allowed-types}")
    private List<String> allowedTypes;

    @Value("${file.upload.allowed-extensions}")
    private List<String> allowedExtensions;

    // Максимальные размеры для разных типов файлов
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024; // 500MB
    private static final long MAX_AUDIO_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long MAX_DOCUMENT_SIZE = 50 * 1024 * 1024; // 50MB

    // Разрешенные MIME типы для разных категорий
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv", "video/webm"
    );

    private static final Set<String> AUDIO_TYPES = Set.of(
            "audio/mp3", "audio/wav", "audio/ogg", "audio/flac", "audio/aac", "audio/m4a"
    );

    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "text/csv", "application/rtf"
    );

    /**
     * Валидировать файл
     */
    public void validateFile(MultipartFile file, FileMetadata.FileType fileType) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("", "Файл не может быть пустым");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new FileValidationException("", "Имя файла не может быть пустым");
        }

        // Проверка размера файла
        validateFileSize(file, fileType, fileName);

        // Проверка типа контента
        validateContentType(file, fileType, fileName);

        // Проверка расширения файла
        validateFileExtension(fileName, fileType);

        // Проверка имени файла на недопустимые символы
        validateFileName(fileName);
    }

    /**
     * Валидировать размер файла
     */
    private void validateFileSize(MultipartFile file, FileMetadata.FileType fileType, String fileName) {
        long fileSize = file.getSize();
        long maxSize = getMaxFileSizeForType(fileType);

        if (fileSize > maxSize) {
            throw new FileValidationException(fileName, 
                    String.format("Размер файла %d bytes превышает максимальный размер %d bytes для типа %s", 
                            fileSize, maxSize, fileType));
        }

        if (fileSize <= 0) {
            throw new FileValidationException(fileName, "Размер файла должен быть больше 0");
        }
    }

    /**
     * Валидировать тип контента
     */
    private void validateContentType(MultipartFile file, FileMetadata.FileType fileType, String fileName) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileValidationException(fileName, "Тип контента не определен");
        }

        Set<String> allowedTypesForCategory = getAllowedTypesForCategory(fileType);
        if (!allowedTypesForCategory.isEmpty() && !allowedTypesForCategory.contains(contentType)) {
            throw new FileValidationException(fileName, 
                    String.format("Тип контента %s не разрешен для типа файла %s", contentType, fileType));
        }

        // Дополнительная проверка для глобального списка разрешенных типов
        if (allowedTypes != null && !allowedTypes.isEmpty() && !allowedTypes.contains(contentType)) {
            throw new FileValidationException(fileName, 
                    String.format("Тип контента %s не в списке разрешенных", contentType));
        }
    }

    /**
     * Валидировать расширение файла
     */
    private void validateFileExtension(String fileName, FileMetadata.FileType fileType) {
        String extension = getFileExtension(fileName).toLowerCase();
        
        Set<String> allowedExtensionsForCategory = getAllowedExtensionsForCategory(fileType);
        if (!allowedExtensionsForCategory.isEmpty() && !allowedExtensionsForCategory.contains(extension)) {
            throw new FileValidationException(fileName, 
                    String.format("Расширение файла %s не разрешено для типа %s", extension, fileType));
        }

        // Дополнительная проверка для глобального списка разрешенных расширений
        if (allowedExtensions != null && !allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
            throw new FileValidationException(fileName, 
                    String.format("Расширение файла %s не в списке разрешенных", extension));
        }
    }

    /**
     * Валидировать имя файла
     */
    private void validateFileName(String fileName) {
        // Проверка на недопустимые символы
        String[] invalidChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String invalidChar : invalidChars) {
            if (fileName.contains(invalidChar)) {
                throw new FileValidationException(fileName, 
                        String.format("Имя файла содержит недопустимый символ: %s", invalidChar));
            }
        }

        // Проверка длины имени файла
        if (fileName.length() > 255) {
            throw new FileValidationException(fileName, "Имя файла слишком длинное (максимум 255 символов)");
        }
    }

    /**
     * Получить максимальный размер файла для типа
     */
    private long getMaxFileSizeForType(FileMetadata.FileType fileType) {
        // Сначала пробуем получить из конфигурации
        try {
            if (maxFileSize != null) {
                return parseSize(maxFileSize);
            }
        } catch (Exception e) {
            // Игнорируем ошибки парсинга, используем значения по умолчанию
        }

        // Значения по умолчанию для разных типов
        switch (fileType) {
            case AVATAR:
                return MAX_AVATAR_SIZE;
            case IMAGE:
            case QUEST_MEDIA:
                return MAX_IMAGE_SIZE;
            case VIDEO:
                return MAX_VIDEO_SIZE;
            case AUDIO:
                return MAX_AUDIO_SIZE;
            case LEVEL_FILE:
            case DOCUMENT:
                return MAX_DOCUMENT_SIZE;
            default:
                return 50 * 1024 * 1024; // 50MB по умолчанию
        }
    }

    /**
     * Получить разрешенные типы контента для категории
     */
    private Set<String> getAllowedTypesForCategory(FileMetadata.FileType fileType) {
        switch (fileType) {
            case AVATAR:
            case IMAGE:
                return IMAGE_TYPES;
            case QUEST_MEDIA:
                // Для медиа файлов квестов разрешены все типы
                Set<String> mediaTypes = new HashSet<>();
                mediaTypes.addAll(IMAGE_TYPES);
                mediaTypes.addAll(VIDEO_TYPES);
                mediaTypes.addAll(AUDIO_TYPES);
                return mediaTypes;
            case VIDEO:
                return VIDEO_TYPES;
            case AUDIO:
                return AUDIO_TYPES;
            case LEVEL_FILE:
            case DOCUMENT:
                return DOCUMENT_TYPES;
            default:
                return Set.of(); // Пустое множество означает любые типы
        }
    }

    /**
     * Получить разрешенные расширения для категории
     */
    private Set<String> getAllowedExtensionsForCategory(FileMetadata.FileType fileType) {
        switch (fileType) {
            case AVATAR:
            case IMAGE:
                return Set.of("jpg", "jpeg", "png", "gif", "webp", "svg");
            case QUEST_MEDIA:
                Set<String> mediaExtensions = new HashSet<>();
                mediaExtensions.addAll(Set.of("jpg", "jpeg", "png", "gif", "webp", "svg"));
                mediaExtensions.addAll(Set.of("mp4", "avi", "mov", "wmv", "flv", "webm"));
                mediaExtensions.addAll(Set.of("mp3", "wav", "ogg", "flac", "aac", "m4a"));
                return mediaExtensions;
            case VIDEO:
                return Set.of("mp4", "avi", "mov", "wmv", "flv", "webm");
            case AUDIO:
                return Set.of("mp3", "wav", "ogg", "flac", "aac", "m4a");
            case LEVEL_FILE:
            case DOCUMENT:
                return Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf");
            default:
                return Set.of(); // Пустое множество означает любые расширения
        }
    }

    /**
     * Получить расширение файла
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Парсить размер из строки (например, "50MB" -> 52428800)
     */
    private long parseSize(String size) {
        size = size.trim().toUpperCase();
        
        if (size.endsWith("KB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
        } else if (size.endsWith("MB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
        } else if (size.endsWith("GB")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(size);
        }
    }

    /**
     * Проверить, является ли файл изображением
     */
    public boolean isImage(String contentType) {
        return IMAGE_TYPES.contains(contentType);
    }

    /**
     * Проверить, является ли файл видео
     */
    public boolean isVideo(String contentType) {
        return VIDEO_TYPES.contains(contentType);
    }

    /**
     * Проверить, является ли файл аудио
     */
    public boolean isAudio(String contentType) {
        return AUDIO_TYPES.contains(contentType);
    }

    /**
     * Проверить, является ли файл документом
     */
    public boolean isDocument(String contentType) {
        return DOCUMENT_TYPES.contains(contentType);
    }
}