package dn.quest.filestorage.exception;

/**
 * Исключение при работе с хранилищем
 */
public class StorageException extends FileStorageException {

    private final String storageType;

    public StorageException(String storageType, String message) {
        super("STORAGE_ERROR", message);
        this.storageType = storageType;
    }

    public StorageException(String storageType, String message, Throwable cause) {
        super("STORAGE_ERROR", message, cause);
        this.storageType = storageType;
    }

    public String getStorageType() {
        return storageType;
    }
}