package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.exceptions.EntityNotFoundException;

@Slf4j
@ControllerAdvice(basePackages = "ru.otus.hw.controllers.page")
@Order(1)
@RequiredArgsConstructor
public class MvcExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.warn("Entity not found in database: {}", ex.getMessage());
        log.warn("handleEntityNotFoundException.URL: {}", request.getDescription(true));
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorText", ex.getMessage());
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleCommonException(Exception ex, WebRequest request) {
        log.error("Exception thrown: {}", ex.getMessage(), ex);
        log.error("handleCommonException.URL: {}", request.getDescription(true));
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorText", "Obviously something went wrong. " +
                                   "Give us time to figure it out...");
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return modelAndView;
    }
}