package ru.pricat.exception;

/**
 * Базовое исключение для ошибок загрузки файлов.
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
