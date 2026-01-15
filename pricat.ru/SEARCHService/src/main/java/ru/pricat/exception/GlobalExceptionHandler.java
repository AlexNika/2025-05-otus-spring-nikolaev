package ru.pricat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка всех непредвиденных ошибок
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model) {
        log.error("Непредвиденная ошибка в контроллере", ex);
        Pageable pageable = PageRequest.of(0, 20);
        model.addAttribute("errorMessage", "Произошла внутренняя ошибка. Пожалуйста, попробуйте позже.");
        model.addAttribute("query", "");
        model.addAttribute("company", "");
        model.addAttribute("results", Page.empty(pageable));
        model.addAttribute("companies", List.of());
        return "search";
    }

    /**
     * Обработка ошибок валидации
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Некорректный запрос: " + ex.getMessage());
        model.addAttribute("query", "");
        model.addAttribute("company", "");
        model.addAttribute("results", List.of());
        model.addAttribute("companies", List.of());
        return "search";
    }
}
