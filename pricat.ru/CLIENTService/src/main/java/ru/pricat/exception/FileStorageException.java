package ru.pricat.exception;

/**
 * Исключение для ошибок при работе с хранилищем файлов (S3).
 */
public class FileStorageException extends FileUploadException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
