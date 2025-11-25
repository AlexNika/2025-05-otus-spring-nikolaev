package ru.pricat.exception;

import java.time.Instant;

/**
 * Запись для представления стандартного формата ответа об ошибке в API Gateway.
 * Структура соответствует используемой в auth-service.
 *
 * @param status    HTTP-код статуса ошибки (например, 400, 404, 500).
 * @param error     Общее описание типа ошибки (например, "Bad Request").
 * @param message   Подробное сообщение об ошибке.
 * @param timestamp Время возникновения ошибки.
 * @param path      Путь, по которому произошла ошибка (может быть null).
 * @param requestId Идентификатор запроса для отслеживания (может быть null).
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path,
        String requestId
) {
    public ErrorResponse(int status, String error, String message, Instant timestamp) {
        this(status, error, message, timestamp, null, null);
    }

    public ErrorResponse(int status, String error, String message, Instant timestamp, String path) {
        this(status, error, message, timestamp, path, null);
    }
}
