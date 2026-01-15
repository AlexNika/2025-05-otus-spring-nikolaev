package ru.pricat.config.security;

import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.pricat.config.properties.AccessTokenConfig;
import ru.pricat.config.properties.AccessTokenExpirationThresholdConfig;
import ru.pricat.config.properties.RefreshTokenConfig;
import ru.pricat.model.dto.auth.LoginResponseDto;
import ru.pricat.service.AuthService;

import java.io.IOException;

/**
 * Фильтр для автоматического обновления JWT токенов.
 * Перехватывает запросы и обновляет access token при его скором истечении
 * с помощью refresh token. Обеспечивает непрерывность пользовательской сессии.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenRefreshFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE = "access-token";

    private static final String REFRESH_TOKEN_COOKIE = "refresh-token";

    private final AuthService authService;

    private final JwtDecoder jwtDecoder;

    private final AccessTokenConfig accessTokenConfig;

    private final RefreshTokenConfig refreshTokenConfig;

    private final AccessTokenExpirationThresholdConfig accessTokenExpirationThresholdConfig;

    /**
     * Основной метод фильтрации запросов.
     * Проверяет необходимость обновления access token и выполняет его при необходимости.
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException при ошибках сервлета
     * @throws IOException при ошибках ввода/вывода
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = getCookieValue(request, ACCESS_TOKEN_COOKIE);
        String refreshToken = getCookieValue(request, REFRESH_TOKEN_COOKIE);
        if (accessToken != null && refreshToken != null && isTokenExpiringSoon(accessToken)) {
            try {
                log.info("Access token expiring soon, attempting refresh");
                LoginResponseDto newTokens = authService.refresh(refreshToken);
                updateTokensInCookies(response, newTokens);
                log.info("Successfully refreshed access token");
            } catch (Exception e) {
                log.warn("Token refresh failed, clearing authentication", e);
                SecurityContextHolder.clearContext();
                clearAuthCookies(response);
                response.sendRedirect("/login?error=session_expired");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет, истекает ли скоро access token.
     *
     * @param token JWT access token
     * @return true если до истечения токена осталось меньше порогового значения
     */
    private boolean isTokenExpiringSoon(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Long exp = jwt.getExpiresAt() != null ? jwt.getExpiresAt().getEpochSecond() : null;
            if (exp == null) {
                return false;
            }
            long currentTime = System.currentTimeMillis() / 1000;
            return (exp - currentTime) < accessTokenExpirationThresholdConfig.getAccessTokenExpirationThreshold();
        } catch (JwtException e) {
            log.warn("Invalid JWT token for expiration check", e);
            return false;
        } catch (Exception e) {
            log.warn("Failed to check token expiration", e);
            return false;
        }
    }

    /**
     * Извлекает значение куки по имени из HTTP запроса.
     *
     * @param request HTTP запрос
     * @param cookieName имя куки
     * @return значение куки или null если не найдена
     */
    @Nullable
    private String getCookieValue(@NonNull HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Обновляет access и refresh токены в куках HTTP ответа.
     *
     * @param response HTTP ответ
     * @param tokens DTO с новыми токенами
     */
    private void updateTokensInCookies(@NonNull HttpServletResponse response, @NonNull LoginResponseDto tokens) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, tokens.accessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(accessTokenConfig.getAccessTokenMaxAge());
        accessTokenCookie.setHttpOnly(false);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, tokens.refreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(refreshTokenConfig.getRefreshTokenMaxAge());
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);
    }

    /**
     * Очищает аутентификационные куки из HTTP ответа.
     *
     * @param response HTTP ответ
     */
    private void clearAuthCookies(@NonNull HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, "");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }

    /**
     * Определяет, должен ли фильтр обрабатывать текущий запрос.
     * Исключает статические ресурсы и публичные endpoints.
     *
     * @param request HTTP запрос
     * @return true если фильтр должен пропустить запрос, иначе false
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return NotFilteredResources.getExcludedPath(request);
    }
}
