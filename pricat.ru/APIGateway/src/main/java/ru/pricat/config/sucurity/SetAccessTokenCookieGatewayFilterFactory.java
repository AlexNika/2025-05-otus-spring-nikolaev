package ru.pricat.config.sucurity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.pricat.config.properties.AccessTokenConfig;

import java.net.URI;
import java.time.Duration;

/**
 * Фабрика фильтра API-Gateway шлюза для извлечения access-токена из JSON-ответа на логин
 * и установки его в HTTP-куки с именем "access-token".
 * Использует ModifyResponseBodyGatewayFilterFactory для модификации тела ответа.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetAccessTokenCookieGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    /**
     * Контекст приложения, используемый для получения бина ModifyResponseBodyGatewayFilterFactory.
     */
    private final ApplicationContext context;

    /**
     * Конфигурация для параметров access-токена, включая время жизни куки.
     */
    private final AccessTokenConfig accessTokenConfig;

    /**
     * Создает фильтр, который модифицирует тело ответа на запрос логина.
     * Извлекает access-токен из JSON и устанавливает его в куки.
     *
     * @param config конфигурация (не используется, передается как Object)
     * @return фильтр шлюза
     */
    @Override
    public GatewayFilter apply(Object config) {
        ModifyResponseBodyGatewayFilterFactory factory = context.getBean(ModifyResponseBodyGatewayFilterFactory.class);
        return factory.apply(new ModifyResponseBodyGatewayFilterFactory.Config()
                .setInClass(JsonNode.class)
                .setOutClass(JsonNode.class)
                .setRewriteFunction(new LoginResponseRewriter()));
    }

    /**
     * Внутренний класс, реализующий логику перезаписи тела ответа.
     * Извлекает access-токен и устанавливает его в куки, используя настройки из внешнего класса.
     */
    private class LoginResponseRewriter implements RewriteFunction<JsonNode, JsonNode> {

        /**
         * Извлекает access-токен из исходного JSON-ответа и добавляет его в HTTP-куки.
         * Время жизни куки берется из конфигурации. Флаг secure устанавливается в зависимости от схемы запроса.
         *
         * @param exchange           объект обмена, содержащий запрос и ответ
         * @param originalResponse   оригинальный JSON-ответ от backend-сервиса
         * @return реактивный объект с оригинальным JSON-ответом
         */
        @Override
        public Mono<JsonNode> apply(ServerWebExchange exchange, JsonNode originalResponse) {
            try {
                String accessToken = originalResponse.get("accessToken").asText();
                Duration maxAge = Duration.ofSeconds(accessTokenConfig.getAccessTokenMaxAge());
                ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                        .from("access-token", accessToken)
                        .maxAge(maxAge)
                        .httpOnly(false)
                        .path("/")
                        .sameSite("Lax");
                URI requestURI = exchange.getRequest().getURI();
                cookieBuilder.secure("https".equalsIgnoreCase(requestURI.getScheme()));
                ResponseCookie cookie = cookieBuilder.build();
                exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());
                return Mono.just(originalResponse);
            } catch (Exception e) {
                log.error("Error rewriting login response", e);
                return Mono.just(originalResponse);
            }
        }
    }
}
