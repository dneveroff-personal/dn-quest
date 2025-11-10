package dn.quest.filestorage.service;

import dn.quest.filestorage.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с файлами
 */
public interface FileStorageService {

    /**
     * Загрузить один файл
     */
    FileResponseDTO uploadFile(MultipartFile file, FileUploadRequestDTO request, String username);

    /**
     * Загрузить несколько файлов
     */
    BatchFileUploadResponseDTO uploadFiles(List<MultipartFile> files, BatchFileUploadRequestDTO request, String username);

    /**
     * Скачать файл
     */
    InputStream downloadFile(UUID fileId, String username);

    /**
     * Скачать файл по имени
     */
    InputStream downloadFileByName(String fileName, String username);

    /**
     * Получить метаданные файла
     */
    FileResponseDTO getFileMetadata(UUID fileId, String username);

    /**
     * Получить метаданные файла по имени
     */
    FileResponseDTO getFileMetadataByName(String fileName, String username);

    /**
     * Поиск файлов
     */
    FileSearchResponseDTO searchFiles(FileSearchRequestDTO request, String username);

    /**
     * Удалить файл
     */
    void deleteFile(UUID fileId, String username);

    /**
     * Удалить файл по имени
     */
    void deleteFileByName(String fileName, String username);

    /**
     * Сгенерировать предписанный URL для скачивания
     */
    String generatePresignedDownloadUrl(UUID fileId, Duration duration, String username);

    /**
     * Сгенерировать предписанный URL для загрузки
     */
    String generatePresignedUploadUrl(String fileName, String contentType, Duration duration, String username);

    /**
     * Копировать файл
     */
    FileResponseDTO copyFile(UUID sourceFileId, FileUploadRequestDTO request, String username);

    /**
     * Переместить файл
     */
    FileResponseDTO moveFile(UUID sourceFileId, FileUploadRequestDTO request, String username);

    /**
     * Обновить метаданные файла
     */
    FileResponseDTO updateFileMetadata(UUID fileId, FileUploadRequestDTO request, String username);

    /**
     * Получить файлы владельца
     */
    Page<FileResponseDTO> getFilesByOwner(UUID ownerId, Pageable pageable, String username);

    /**
     * Получить публичные файлы
     */
    Page<FileResponseDTO> getPublicFiles(Pageable pageable);

    /**
     * Получить временные файлы
     */
    Page<FileResponseDTO> getTemporaryFiles(Pageable pageable, String username);

    /**
     * Очистить истекшие временные файлы
     */
    void cleanupExpiredTemporaryFiles();

    /**
     * Получить статистику хранилища
     */
    StorageStatisticsDTO getStorageStatistics();

    /**
     * Получить статистику пользователя
     */
    UserStorageStatisticsDTO getUserStorageStatistics(UUID userId, String username);

    /**
     * Проверить доступ к файлу
     */
    boolean hasFileAccess(UUID fileId, String username);

    /**
     * Проверить, является ли файл публичным
     */
    boolean isFilePublic(UUID fileId);
}