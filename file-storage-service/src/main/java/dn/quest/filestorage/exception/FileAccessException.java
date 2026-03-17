package dn.quest.filestorage.exception;

import java.util.UUID;

/**
 * Исключение при доступе к файлу
 */
public class FileAccessException extends FileStorageException {

    private final UUID fileId;
    private final String fileName;

    public FileAccessException(UUID fileId, String message) {
        super("FILE_ACCESS_DENIED", message);
        this.fileId = fileId;
        this.fileName = null;
    }

    public FileAccessException(String fileName, String message) {
        super("FILE_ACCESS_DENIED", message);
        this.fileId = null;
        this.fileName = fileName;
    }

    public FileAccessException(UUID fileId, String fileName, String message) {
        super("FILE_ACCESS_DENIED", message);
        this.fileId = fileId;
        this.fileName = fileName;
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }
}