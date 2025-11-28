package ru.pricat.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для проверки X-API-Key заголовка для внутренних API endpoints.
 * Используется для аутентификации запросов между микросервисами.
 */
@Slf4j
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String validApiKey;

    /**
     * Основной метод фильтрации запросов.
     * Проверяет X-API-Key заголовка, использующегося для аутентификации запросов между микросервисами.
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException при ошибках сервлета
     * @throws IOException при ошибках ввода/вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/internal/")) {
            String apiKey = request.getHeader("X-API-Key");
            log.debug("Checking API key for internal endpoint: {}", path);
            log.debug("Received API key: {}, Expected: {}", apiKey, validApiKey);
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Missing X-API-Key header for internal endpoint: {}", path);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing X-API-Key header");
                return;
            }
            if (!apiKey.equals(validApiKey)) {
                log.warn("Invalid X-API-Key for internal endpoint: {}. Received: '{}', Expected: '{}'",
                        path, apiKey, validApiKey);
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid API key");
                return;
            }
            log.debug("API key validation successful for: {}", path);
        }
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(@NonNull HttpServletResponse response, @NonNull HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String errorJson = String.format(
                "{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message,
                java.time.Instant.now()
        );
        response.getWriter().write(errorJson);
    }
}