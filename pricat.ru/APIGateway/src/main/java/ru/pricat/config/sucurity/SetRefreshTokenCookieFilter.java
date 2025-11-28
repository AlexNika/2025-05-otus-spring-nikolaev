package ru.pricat.config.sucurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.pricat.config.properties.RefreshTokenConfig;

import java.net.URI;
import java.time.Duration;

import static ru.pricat.util.AppConstants.API_V1_AUTH_PATH;

/**
 * Фильтр API-Gateway шлюза для обработки ответа от эндпоинта /auth/login.
 * Извлекает заголовок X-Refresh-Token, устанавливает его в httpOnly куки
 * и удаляет заголовок из ответа, чтобы клиент его не видел напрямую.
 * Флаг secure для куки устанавливается в зависимости от схемы исходного запроса (https или нет).
 */
@Slf4j
@Component
public class SetRefreshTokenCookieFilter extends AbstractGatewayFilterFactory<SetRefreshTokenCookieFilter.Config> {

    /**
     * Конфигурация для параметров refresh-токена, включая время жизни куки.
     */
    private final RefreshTokenConfig refreshTokenConfig;

    /**
     * Конструктор фильтра, инициализирующий конфигурацию.
     *
     * @param refreshTokenConfig конфигурация параметров refresh-токена
     */
    public SetRefreshTokenCookieFilter(RefreshTokenConfig refreshTokenConfig) {
        super(Config.class);
        this.refreshTokenConfig = refreshTokenConfig;
    }

    /**
     * Применяет логику фильтра к цепочке обработки запроса.
     * После фильтрации проверяет, является ли запрос к /auth/login и успешен ли ответ.
     * Если да, извлекает refresh-токен из заголовка, устанавливает его в куки и удаляет заголовок.
     *
     * @param config конфигурация фильтра (в данном случае пустая)
     * @return фильтр шлюза
     */

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) ->
                chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    URI requestURI = exchange.getRequest().getURI();
                    if (requestURI.getPath().equals(API_V1_AUTH_PATH + "/login") &&
                        response.getStatusCode() != null &&
                        response.getStatusCode().is2xxSuccessful()) {
                        String refreshToken = response.getHeaders().getFirst("X-Refresh-Token");
                        if (refreshToken != null) {
                            log.debug("Setting httpOnly cookie for refresh token");
                            response.getHeaders().remove("X-Refresh-Token");
                            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                                    .from("refreshToken", refreshToken)
                                    .httpOnly(true)
                                    .sameSite("Lax")
                                    .path("/")
                                    .maxAge(Duration.ofSeconds(refreshTokenConfig.getRefreshTokenMaxAge()));
                            cookieBuilder.secure("https".equalsIgnoreCase(requestURI.getScheme()));
                            response.addCookie(cookieBuilder.build());
                        } else {
                            log.warn("X-Refresh-Token header was expected but not found in /login response");
                        }
                    }
                }));
    }

    /**
     * Класс конфигурации для фильтра. В данном случае пустой, так как фильтр не требует настроек.
     */
    public static class Config {
    }
}