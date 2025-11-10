package dn.quest.filestorage.exception;

/**
 * Исключение при валидации файла
 */
public class FileValidationException extends FileStorageException {

    private final String fileName;
    private final String validationError;

    public FileValidationException(String fileName, String validationError) {
        super("FILE_VALIDATION_ERROR", "Ошибка валидации файла " + fileName + ": " + validationError);
        this.fileName = fileName;
        this.validationError = validationError;
    }

    public String getFileName() {
        return fileName;
    }

    public String getValidationError() {
        return validationError;
    }
}