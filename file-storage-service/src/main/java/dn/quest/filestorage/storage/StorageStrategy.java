package dn.quest.filestorage.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * Интерфейс стратегии хранения файлов
 */
public interface StorageStrategy {

    /**
     * Сохранить файл
     *
     * @param file     файл для сохранения
     * @param path     путь в хранилище
     * @param metadata дополнительные метаданные
     * @return путь к сохраненному файлу
     */
    String storeFile(MultipartFile file, String path, Map<String, String> metadata);

    /**
     * Сохранить файл из потока
     *
     * @param inputStream поток данных файла
     * @param fileName   имя файла
     * @param contentType тип контента
     * @param path       путь в хранилище
     * @param metadata   дополнительные метаданные
     * @return путь к сохраненному файлу
     */
    String storeFile(InputStream inputStream, String fileName, String contentType, 
                    String path, Map<String, String> metadata);

    /**
     * Загрузить файл
     *
     * @param path путь к файлу
     * @return поток данных файла
     */
    InputStream loadFile(String path);

    /**
     * Удалить файл
     *
     * @param path путь к файлу
     * @return true если файл успешно удален
     */
    boolean deleteFile(String path);

    /**
     * Проверить существование файла
     *
     * @param path путь к файлу
     * @return true если файл существует
     */
    boolean fileExists(String path);

    /**
     * Получить размер файла
     *
     * @param path путь к файлу
     * @return размер файла в байтах
     */
    long getFileSize(String path);

    /**
     * Сгенерировать предписанный URL для доступа к файлу
     *
     * @param path     путь к файлу
     * @param duration время действия URL
     * @return предписанный URL
     */
    String generatePresignedUrl(String path, Duration duration);

    /**
     * Сгенерировать предписанный URL для загрузки файла
     *
     * @param path     путь к файлу
     * @param contentType тип контента
     * @param duration время действия URL
     * @return предписанный URL для загрузки
     */
    String generatePresignedUploadUrl(String path, String contentType, Duration duration);

    /**
     * Копировать файл
     *
     * @param sourcePath исходный путь
     * @param targetPath целевой путь
     * @return true если файл успешно скопирован
     */
    boolean copyFile(String sourcePath, String targetPath);

    /**
     * Переместить файл
     *
     * @param sourcePath исходный путь
     * @param targetPath целевой путь
     * @return true если файл успешно перемещен
     */
    boolean moveFile(String sourcePath, String targetPath);

    /**
     * Получить метаданные файла
     *
     * @param path путь к файлу
     * @return метаданные файла
     */
    Map<String, String> getFileMetadata(String path);

    /**
     * Обновить метаданные файла
     *
     * @param path     путь к файлу
     * @param metadata новые метаданные
     * @return true если метаданные успешно обновлены
     */
    boolean updateFileMetadata(String path, Map<String, String> metadata);

    /**
     * Получить тип хранилища
     *
     * @return тип хранилища
     */
    String getStorageType();

    /**
     * Проверить доступность хранилища
     *
     * @return true если хранилище доступно
     */
    boolean isStorageAvailable();

    /**
     * Получить статистику хранилища
     *
     * @return статистика использования хранилища
     */
    StorageStatistics getStorageStatistics();

    /**
     * Инициализировать хранилище
     */
    void initializeStorage();

    /**
     * Очистить временные файлы
     */
    void cleanupTempFiles();
}