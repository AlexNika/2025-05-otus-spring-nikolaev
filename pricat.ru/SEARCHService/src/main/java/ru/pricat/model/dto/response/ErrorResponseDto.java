package ru.pricat.model.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Стандартизированный формат ответа об ошибке для REST API.
 * Используется для единообразной обработки ошибок во всех контроллерах.
 *
 * @param status    HTTP статус код
 * @param error     Тип ошибки (например, "Not Found", "Conflict")
 * @param message   Детальное сообщение об ошибке
 * @param timestamp Время возникновения ошибки
 * @param path      URL путь запроса
 * @param requestId Уникальный идентификатор запроса для трассировки
 */
public record ErrorResponseDto(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path,
        String requestId
) {
    /**
     * Создает ErrorResponseDto с автоматически сгенерированным requestId и текущим временем.
     */
    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(
                status,
                error,
                message,
                Instant.now(),
                path,
                UUID.randomUUID().toString()
        );
    }

    /**
     * Создает ErrorResponseDto с существующим requestId (для цепочки вызовов).
     */
    public static ErrorResponseDto of(int status, String error, String message, String path, String requestId) {
        return new ErrorResponseDto(
                status,
                error,
                message,
                Instant.now(),
                path,
                requestId
        );
    }
}
