package ru.pricat.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для Web UI (Thymeleaf).
 * Обрабатывает исключения и возвращает пользовательские страницы ошибок.
 * Дополняет GlobalExceptionHandler, который обрабатывает REST API исключения.
 */
@Slf4j
@ControllerAdvice
public class WebExceptionHandler {

    /**
     * Обрабатывает ошибки валидации данных в формах.
     * Собирает все ошибки валидации и возвращает на исходную страницу.
     *
     * @param ex исключение валидации
     * @param request HTTP запрос
     * @param redirectAttributes атрибуты для редиректа
     * @return строка редиректа на исходную страницу
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(@NonNull MethodArgumentNotValidException ex,
                                             @NonNull HttpServletRequest request,
                                             @NonNull RedirectAttributes redirectAttributes) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation error for {}: {}", request.getRequestURI(), errorMessage);
        redirectAttributes.addFlashAttribute("error", "Validation Error");
        redirectAttributes.addFlashAttribute("message", errorMessage);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/login");
    }

    /**
     * Обрабатывает ошибки сервиса аутентификации.
     * Перенаправляет на страницу логина с сообщением об ошибке.
     *
     * @param ex исключение сервиса аутентификации
     * @param request HTTP запрос
     * @param redirectAttributes атрибуты для редиректа
     * @return строка редиректа на страницу логина
     */
    @ExceptionHandler(AuthServiceException.class)
    public String handleAuthServiceException(@NonNull AuthServiceException ex,
                                             @NonNull HttpServletRequest request,
                                             @NonNull RedirectAttributes redirectAttributes) {
        log.warn("Auth service error for {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Authentication Error");
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/login";
    }

    /**
     * Обрабатывает ситуации, когда пользователь не найден.
     * Возвращает страницу ошибки 404.
     *
     * @param ex исключение "пользователь не найден"
     * @param request HTTP запрос
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(@NonNull UserNotFoundException ex,
                                              HttpServletRequest request,
                                              @NonNull Model model) {
        log.warn("User not found: {}", ex.getMessage());
        model.addAttribute("error", "User Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Обрабатывает конфликты уникальности email при регистрации.
     * Перенаправляет на страницу регистрации с сообщением об ошибке.
     *
     * @param ex исключение "email не уникален"
     * @param request HTTP запрос
     * @param redirectAttributes атрибуты для редиректа
     * @return строка редиректа на страницу регистрации
     */
    @ExceptionHandler(EmailNotUniqueException.class)
    public String handleEmailNotUniqueException(@NonNull EmailNotUniqueException ex,
                                                HttpServletRequest request,
                                                @NonNull RedirectAttributes redirectAttributes) {
        log.warn("Email conflict: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Registration Error");
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/register";
    }

    /**
     * Обрабатывает ошибки недоступности сервиса.
     * Возвращает страницу ошибки 503.
     *
     * @param ex исключение "сервис недоступен"
     * @param request HTTP запрос
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public String handleServiceUnavailableException(@NonNull ServiceUnavailableException ex,
                                                    @NonNull HttpServletRequest request,
                                                    @NonNull Model model) {
        log.error("Service unavailable: {}", ex.getMessage());
        model.addAttribute("error", "Service Unavailable");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Обрабатывает ошибки валидации файлов.
     * Перенаправляет на страницу загрузки файлов с сообщением об ошибке.
     *
     * @param ex исключение валидации файла
     * @param request HTTP запрос
     * @param redirectAttributes атрибуты для редиректа
     * @return строка редиректа на страницу загрузки файлов
     */
    @ExceptionHandler(FileValidationException.class)
    public String handleFileValidationException(@NonNull FileValidationException ex,
                                                @NonNull HttpServletRequest request,
                                                @NonNull RedirectAttributes redirectAttributes) {
        log.warn("File validation error for {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "File Validation Error");
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/files";
    }

    /**
     * Обрабатывает ошибки хранилища файлов.
     * Перенаправляет на страницу загрузки файлов с сообщением об ошибке.
     *
     * @param ex исключение хранилища файлов
     * @param request HTTP запрос
     * @param redirectAttributes атрибуты для редиректа
     * @return строка редиректа на страницу загрузки файлов
     */
    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(@NonNull FileStorageException ex,
                                             @NonNull HttpServletRequest request,
                                             @NonNull RedirectAttributes redirectAttributes) {
        log.error("File storage error for {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "File Storage Error");
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/files";
    }

    /**
     * Обрабатывает общие ошибки загрузки файлов.
     * Перенаправляет на страницу загрузки файлов с сообщением об ошибке.
     *
     * @param ex исключение загрузки файла
     * @param request HTTP запрос
     * @param redirectAttributes атрибуты для редиректа
     * @return строка редиректа на страницу загрузки файлов
     */
    @ExceptionHandler(FileUploadException.class)
    public String handleFileUploadException(@NonNull FileUploadException ex,
                                            @NonNull HttpServletRequest request,
                                            @NonNull RedirectAttributes redirectAttributes) {
        log.error("File upload error for {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "File Upload Error");
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/files";
    }

    /**
     * Обрабатывает ситуацию, когда у пользователя нет прав на доступ к ресурсу.
     * Возвращает страницу ошибки 403.
     *
     * @param ex исключение доступа
     * @param request HTTP запрос
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(@NonNull AccessDeniedException ex,
                                              @NonNull HttpServletRequest request,
                                              @NonNull Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())
                ? auth.getName() : "Anonymous";

        log.warn("Access denied to {} for user {}: {}", request.getRequestURI(), username, ex.getMessage());

        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", "You don't have permission to access this resource.");
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        model.addAttribute("path", request.getRequestURI());

        return "error";
    }

    /**
     * Обрабатывает все непредвиденные исключения.
     * Возвращает общую страницу ошибки 500.
     *
     * @param ex исключение
     * @param request HTTP запрос
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unexpected error for request: {}", request.getRequestURI(), ex);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred");
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }
}
