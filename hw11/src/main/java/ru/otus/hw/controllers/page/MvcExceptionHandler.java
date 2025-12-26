package ru.otus.hw.controllers.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;
import ru.otus.hw.exceptions.EntityNotFoundException;

@Slf4j
@ControllerAdvice(basePackages = "ru.otus.hw.controllers.page")
@Order(1)
public class MvcExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFoundException(
            EntityNotFoundException ex,
            Model model,
            ServerWebExchange exchange
    ) {
        log.warn("Entity not found in database: {}", ex.getMessage());
        log.warn("handleEntityNotFoundException.URL: {}", exchange.getRequest().getURI());
        model.addAttribute("errorText", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleCommonException(
            Exception ex,
            Model model,
            ServerWebExchange exchange
    ) {
        log.error("Exception thrown: {}", ex.getMessage(), ex);
        log.error("handleCommonException.URL: {}", exchange.getRequest().getURI());
        model.addAttribute("errorText", "Obviously something went wrong. " +
                                        "Give us time to figure it out...");
        return "error";
    }
}