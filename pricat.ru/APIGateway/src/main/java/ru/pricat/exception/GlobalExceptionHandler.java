package ru.pricat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Глобальный обработчик ошибок для API-Gateway.
 * Перехватывает все необработанные исключения и возвращает стандартизированный JSON-ответ об ошибке.
 * Имеет высокий приоритет (-2), чтобы перехватывать ошибки до стандартного обработчика Spring Boot.
 */
@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    /**
     * Конструктор для инициализации обработчика ошибок с необходимыми зависимостями.
     *
     * @param errorAttributes    компонент для извлечения атрибутов ошибки
     * @param webProperties      настройки веб-слоя
     * @param applicationContext контекст приложения
     * @param configurer         конфигурация кодеков для сериализации ответов
     */
    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webProperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    /**
     * Определяет функцию маршрутизации для обработки всех ошибок.
     * Любая ошибка будет передана в метод {@link #renderErrorResponse(ServerRequest)}.
     *
     * @param errorAttributes компонент для извлечения атрибутов ошибки
     * @return функция маршрутизации, обрабатывающая все запросы на ошибки
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * Формирует и возвращает стандартный JSON-ответ с информацией об ошибке.
     * Извлекает атрибуты ошибки, логирует её, определяет HTTP-статус и формирует объект {@link ErrorResponse}.
     *
     * @param request запрос, вызвавший ошибку
     * @return реактивный ответ с ошибкой в формате JSON
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        String requestPath = request.path();
        String requestId = request.headers().firstHeader("X-Request-Id");
        log.error("Global error handler caught an exception for request: {} {} with X-Request-Id: {}",
                request.method(), requestPath, requestId, getError(request));
        HttpStatus status = determineStatus(errorPropertiesMap, getError(request));
        String message = (String) errorPropertiesMap.getOrDefault("message",
                "An unexpected error occurred");
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                requestPath,
                requestId
        );
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    /**
     * Определяет HTTP-статус ошибки на основе атрибутов ошибки или самого исключения.
     * Пытается получить статус из атрибутов ошибки, затем из исключения типа ResponseStatusException.
     * Если оба способа неудачны, возвращает 500 (INTERNAL_SERVER_ERROR).
     *
     * @param errorAttributes атрибуты ошибки, извлеченные из запроса
     * @param error           исключение, вызвавшее ошибку
     * @return HTTP-статус, соответствующий ошибке
     */
    private HttpStatus determineStatus(Map<String, Object> errorAttributes, Throwable error) {
        Integer statusCodeFromAttributes = (Integer) errorAttributes.get("status");
        if (statusCodeFromAttributes != null) {
            try {
                return HttpStatus.valueOf(statusCodeFromAttributes);
            } catch (IllegalArgumentException ex) {
                log.warn("Unknown HTTP status code from error attributes: {}", statusCodeFromAttributes);
            }
        }
        if (error instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}