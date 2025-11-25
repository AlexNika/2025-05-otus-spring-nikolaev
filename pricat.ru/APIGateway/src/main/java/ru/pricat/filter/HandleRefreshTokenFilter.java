package ru.pricat.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.pricat.config.properties.RefreshTokenConfig;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static ru.pricat.util.AppConstants.API_V1_AUTH_PATH;

/**
 * Фильтр для безопасной обработки обновления токена через /api/v1/auth/refresh.
 * Извлекает refreshToken из httpOnly cookie, передаёт в заголовке, обновляет куку.
 * Гарантирует, что токен не попадает в браузер.
 */
@Slf4j
@Component
public class HandleRefreshTokenFilter extends AbstractGatewayFilterFactory<HandleRefreshTokenFilter.Config> {

    private static final String TOKEN_PATTERN = "^[a-zA-Z0-9-_]{16,}$";

    private final RefreshTokenConfig refreshTokenConfig;

    public HandleRefreshTokenFilter(RefreshTokenConfig refreshTokenConfig) {
        super(Config.class);
        this.refreshTokenConfig = refreshTokenConfig;
    }

    /**
     * Создаёт фильтр, реализующий логику обработки refreshToken:
     * - На этапе запроса: извлекает refreshToken из куки и добавляет в заголовок.
     * - На этапе ответа: удаляет заголовок X-Refresh-Token (всегда) и при успехе
     *   устанавливает новую httpOnly куку.
     *
     * @param config конфигурация (не используется)
     * @return GatewayFilter с безопасной обработкой токенов
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerWebExchange mutatedExchange = exchange;
            Set<URI> originalUris = exchange.getAttribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
            if (originalUris == null || originalUris.isEmpty()) {
                return chain.filter(exchange);
            }
            String originalPath = originalUris.iterator().next().getPath();
            if ((API_V1_AUTH_PATH + "/refresh").equals(originalPath)) {
                log.debug("Handling /api/v1/auth/refresh request");
                HttpCookie refreshTokenCookie = exchange.getRequest().getCookies().getFirst("refreshToken");
                if (refreshTokenCookie != null) {
                    String refreshTokenValue = refreshTokenCookie.getValue();
                    if (isValidToken(refreshTokenValue)) {
                        log.debug("Valid refreshToken found, forwarding in X-Refresh-Token header");
                        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                                .header("X-Refresh-Token", refreshTokenValue)
                                .build();
                        mutatedExchange = exchange.mutate().request(newRequest).build();
                    } else {
                        log.warn("Invalid or suspicious refreshToken format, will not forward");
                    }
                } else {
                    log.warn("No refreshToken found in cookie for /refresh request");
                }
            }

            ServerWebExchange finalMutatedExchange = mutatedExchange;
            return chain.filter(finalMutatedExchange)
                .doOnSuccess(_ -> {
                    ServerHttpResponse response = finalMutatedExchange.getResponse();
                    if (response.getHeaders().containsKey("X-Refresh-Token")) {
                        log.debug("Removing X-Refresh-Token header from response");
                        response.getHeaders().remove("X-Refresh-Token");
                    }
                })
                .doOnError(_ -> {
                    ServerHttpResponse response = finalMutatedExchange.getResponse();
                    if (response.getHeaders().containsKey("X-Refresh-Token")) {
                        log.debug("Removing X-Refresh-Token header from response on error");
                        response.getHeaders().remove("X-Refresh-Token");
                    }
                })
                .then(Mono.fromRunnable(() -> {
                    if ((API_V1_AUTH_PATH + "/refresh").equals(originalPath) &&
                        finalMutatedExchange.getResponse().getStatusCode() != null &&
                        finalMutatedExchange.getResponse().getStatusCode().is2xxSuccessful()) {
                        String newRefreshToken = finalMutatedExchange
                                .getResponse()
                                .getHeaders()
                                .getFirst("X-Refresh-Token");
                        if (newRefreshToken != null) {
                            log.debug("Setting new httpOnly refreshToken cookie");
                            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                                    .from("refreshToken", newRefreshToken)
                                    .httpOnly(true)
                                    .sameSite("Lax")
                                    .path("/")
                                    .maxAge(Duration.ofSeconds(refreshTokenConfig.getRefreshTokenMaxAge()));
                            if (finalMutatedExchange.getRequest().getURI().getScheme().equals("https")) {
                                cookieBuilder.secure(true);
                            }
                            finalMutatedExchange.getResponse().addCookie(cookieBuilder.build());
                        } else {
                            log.warn("X-Refresh-Token header was expected but not found in /refresh response");
                        }
                    }
                }));
        };
    }

    /**
     * Проверяет, что refreshToken соответствует формату: только буквенно-цифровые символы, -_ и длина >=16.
     *
     * @param token значение токена
     * @return true, если токен безопасен
     */
    protected boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return token.matches(TOKEN_PATTERN);
    }

    public static class Config {
    }
}