package ru.pricat.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.HtmlUtils;
import ru.pricat.model.dto.response.ErrorResponseDto;

/**
 * Глобальный обработчик исключений для REST контроллеров.
 * Преобразует исключения в стандартизированный формат ErrorResponseDto.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибки недоступности сервиса/БД.
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleServiceUnavailable(
            ServiceUnavailableException ex,
            HttpServletRequest request) {
        log.error("Service unavailable: {}", ex.getMessage());
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                ex.getMessage(),
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponseDto);
    }


    /**
     * Обрабатывает случаи, когда пользователь не найден.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponseDto);
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleFileValidationException(
            FileValidationException ex,
            HttpServletRequest request) {
        log.warn("File validation error: {}", ex.getMessage());
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST.value(),
                "File storage error",
                ex.getMessage(),
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request) {
        log.error("File storage error: {}", ex.getMessage());
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "File storage error",
                ex.getMessage(),
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDto);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponseDto> handleFileUploadException(
            FileUploadException ex,
            HttpServletRequest request) {
        log.error("File upload error: {}", ex.getMessage());
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "File upload error",
                ex.getMessage(),
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDto);
    }

    /**
     * Обрабатывает конфликты с уникальностью email.
     */
    @ExceptionHandler(EmailNotUniqueException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailNotUnique(
            EmailNotUniqueException ex,
            HttpServletRequest request) {
        log.warn("Email conflict: {}", ex.getMessage());
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponseDto);
    }

    /**
     * Обрабатывает все непредвиденные исключения.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponseDto errorResponseDto = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                HtmlUtils.htmlEscape(request.getRequestURI())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDto);
    }
}
