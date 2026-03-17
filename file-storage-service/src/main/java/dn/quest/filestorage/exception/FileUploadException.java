package dn.quest.filestorage.exception;

/**
 * Исключение при загрузке файла
 */
public class FileUploadException extends FileStorageException {

    private final String fileName;

    public FileUploadException(String fileName, String message) {
        super("FILE_UPLOAD_ERROR", message);
        this.fileName = fileName;
    }

    public FileUploadException(String fileName, String message, Throwable cause) {
        super("FILE_UPLOAD_ERROR", message, cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}