package ru.pricat.exception;

/**
 * Исключение для ошибок валидации файлов.
 * Например: неверный формат, превышение размера и т.д.
 */
public class FileValidationException extends FileUploadException {

    public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}