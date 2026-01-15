package ru.otus.hw.controllers.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "ru.otus.hw.controllers.rest")
@Order(1)
public class WebFluxRestExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<RestExceptionHandler.ValidationErrorResponse> handleWebExchangeBindException(
            WebExchangeBindException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors (WebFlux): {}", errors);
        RestExceptionHandler.ValidationErrorResponse errorResponse =
                new RestExceptionHandler.ValidationErrorResponse("Ошибка валидации", errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<RestExceptionHandler.ErrorResponse> handleServerWebInputException(
            ServerWebInputException ex
    ) {
        log.warn("Bad request - web input exception: {}", ex.getMessage());
        RestExceptionHandler.ErrorResponse errorResponse =
                new RestExceptionHandler.ErrorResponse("Некорректный формат запроса");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}