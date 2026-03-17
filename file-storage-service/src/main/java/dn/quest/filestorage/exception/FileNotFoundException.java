package dn.quest.filestorage.exception;

import java.util.UUID;

/**
 * Исключение, когда файл не найден
 */
public class FileNotFoundException extends FileStorageException {

    private final UUID fileId;
    private final String fileName;

    public FileNotFoundException(UUID fileId) {
        super("FILE_NOT_FOUND", "Файл с ID " + fileId + " не найден");
        this.fileId = fileId;
        this.fileName = null;
    }

    public FileNotFoundException(String fileName) {
        super("FILE_NOT_FOUND", "Файл с именем " + fileName + " не найден");
        this.fileId = null;
        this.fileName = fileName;
    }

    public FileNotFoundException(UUID fileId, String fileName) {
        super("FILE_NOT_FOUND", "Файл с ID " + fileId + " и именем " + fileName + " не найден");
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