package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.exceptions.EntityNotFoundException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        log.warn("handleAccessDeniedException.URL: {}", request.getDescription(true));
        return new ModelAndView("error").addObject("errorText",
                "Доступ запрещен! У вас нет прав для доступа к этой странице.");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.warn("Entity not found in database: {}", ex.getMessage());
        log.warn("handleEntityNotFoundException.URL: {}", request.getDescription(true));
        return new ModelAndView("error").addObject("errorText", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleCommonException(Exception ex, WebRequest request) {
        log.error("Exception thrown: {}", ex.getMessage());
        log.error("handleCommonException.URL: {}", request.getDescription(true));
        return new ModelAndView("error").addObject("errorText",
                "Obviously something went wrong. Give us time to figure it out...");
    }
}