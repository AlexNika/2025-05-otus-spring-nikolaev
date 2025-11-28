package ru.pricat.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.pricat.model.dto.response.ErrorResponseDto;

import java.io.IOException;

/**
 * Кастомная точка входа аутентификации для обработки 401 ошибок.
 * Обеспечивает различную обработку для API запросов (JSON) и Web UI (редирект на логин).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает ошибки аутентификации для защищенных ресурсов.
     * Разделяет обработку для API и Web UI запросов.
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param authException исключение аутентификации
     * @throws IOException при ошибках ввода/вывода
     * @throws ServletException при ошибках сервлета
     */
    @Override
    public void commence(@NonNull HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.warn("Authentication failed for request: {} - {}", request.getMethod(), request.getRequestURI());
        if (isApiRequest(request)) {
            handleApiAuthenticationFailure(response, request, authException);
        } else {
            handleWebAuthenticationFailure(response, request, authException);
        }
    }

    /**
     * Обрабатывает ошибки аутентификации для API запросов.
     * Возвращает стандартизированный JSON ответ с ошибкой 401.
     *
     * @param response HTTP ответ
     * @param request HTTP запрос
     * @param authException исключение аутентификации
     * @throws IOException при ошибках сериализации JSON
     */
    private void handleApiAuthenticationFailure(@NonNull HttpServletResponse response,
                                                @NonNull HttpServletRequest request,
                                                @NonNull AuthenticationException authException) throws IOException {
        String path = request.getServletPath();
        log.info("Handle Api authentication failure for path: {} and AuthException: {}", path,
                authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication required",
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    /**
     * Обрабатывает ошибки аутентификации для Web UI запросов.
     * Возвращает HTML страницу с кнопкой для редиректа на логин через api-gateway.
     *
     * @param response HTTP ответ
     * @param request HTTP запрос
     * @param authException исключение аутентификации
     * @throws IOException при ошибках ввода/вывода
     */
    private void handleWebAuthenticationFailure(@NonNull HttpServletResponse response,
                                                @NonNull HttpServletRequest request,
                                                @NonNull AuthenticationException authException) throws IOException {
        String path = request.getServletPath();
        log.info("Handle Web authentication failure for path: {} and AuthException: {}", path,
                authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("""
                <html>
                <head><title>Unauthorized</title></head>
                <body>
                <h1>401 Unauthorized</h1>
                <p>Please log in to access this page.</p>
                <button onclick="redirectToLogin()">Go to Login</button>
                <script>
                    function redirectToLogin() {
                        window.location.href = 'http://localhost:8080/login';
                    }
                </script>
                </body>
                </html>
                """);
    }

    /**
     * Определяет, является ли запрос API запросом.
     * Проверяет путь запроса и заголовок Accept.
     *
     * @param request HTTP запрос
     * @return true если это API запрос, иначе false
     */
    private boolean isApiRequest(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String acceptHeader = request.getHeader("Accept");
        return path.startsWith("/api") ||
               (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
    }
}
