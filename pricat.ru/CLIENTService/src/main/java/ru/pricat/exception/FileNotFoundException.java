package ru.pricat.exception;

/**
 * Исключение для случаев, когда файл не найден.
 */
public class FileNotFoundException extends FileUploadException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
