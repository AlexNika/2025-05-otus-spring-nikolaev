package ru.pricat.controller.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для обработки ошибок на уровне сервлета.
 * Обрабатывает 404 ошибки и другие ошибки, которые не перехватываются обработчиками исключений.
 * Реализует стандартный интерфейс ErrorController Spring Boot.
 */
@Slf4j
@Controller
public class ErrorPageController implements ErrorController {

    /**
     * Обрабатывает все ошибки, которые не были перехвачены другими обработчиками.
     * Определяет тип ошибки по статус коду и возвращает соответствующую страницу.
     *
     * @param request HTTP запрос с атрибутами ошибки
     * @param model модель для передачи данных в представление
     * @return имя шаблона для отображения ошибки
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        if (requestUri == null) {
            requestUri = "Unknown URL";
        }
        log.warn("Error {} for request: {} - {}", statusCode, requestUri, errorMessage);
        model.addAttribute("status", statusCode);
        model.addAttribute("path", requestUri);
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("error", "Page Not Found");
            model.addAttribute("message", "The page you are looking for does not exist.");
            return "error/error-404";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            model.addAttribute("error", "Access Denied");
            model.addAttribute("message", "You don't have permission to access this resource.");
            return "error/error";
        } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            model.addAttribute("error", "Unauthorized");
            model.addAttribute("message", "Please log in to access this resource.");
            return "error/error";
        } else {
            model.addAttribute("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
            model.addAttribute("message", errorMessage != null ? errorMessage : "An error occurred");
            return "error/error";
        }
    }
}
