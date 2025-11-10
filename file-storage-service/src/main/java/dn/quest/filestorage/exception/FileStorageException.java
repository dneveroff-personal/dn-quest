package dn.quest.filestorage.exception;

/**
 * Базовое исключение для File Storage Service
 */
public class FileStorageException extends RuntimeException {

    private final String errorCode;

    public FileStorageException(String message) {
        super(message);
        this.errorCode = "FILE_STORAGE_ERROR";
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FILE_STORAGE_ERROR";
    }

    public FileStorageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FileStorageException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}