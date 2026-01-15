package ru.pricat.config.sucurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Фильтр API-Gateway микросервиса для добавления заголовка X-Request-Id ко всем входящим и исходящим запросам.
 * Используется для трассировки запросов между микросервисами.
 */
@Slf4j
@Component
public class AddXRequestIdFilter extends AbstractGatewayFilterFactory<AddXRequestIdFilter.Config> {

    /**
     * Конструктор фильтра, инициализирующий конфигурационный класс.
     */
    public AddXRequestIdFilter() {
        super(Config.class);
    }

    /**
     * Применяет логику фильтра к цепочке обработки запроса.
     * Генерирует уникальный идентификатор запроса, добавляет его как заголовок к входящему запросу
     * и к исходящему ответу.
     *
     * @param config конфигурация фильтра (в данном случае пустая)
     * @return фильтр, который добавляет заголовок X-Request-Id
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
            if (requestId == null) {
                requestId = UUID.randomUUID().toString();
                log.debug("Generated new X-Request-Id: {}", requestId);
            } else {
                log.debug("Using existing X-Request-Id from request: {}", requestId);
            }
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-Request-Id", requestId)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();
            String finalRequestId = requestId;
            return chain.filter(mutatedExchange)
                    .then(Mono.fromRunnable(() -> {
                        ServerHttpResponse response = mutatedExchange.getResponse();
                        response.getHeaders().add("X-Request-Id", finalRequestId);
                        log.debug("Added X-Request-Id {} to response", finalRequestId);
                    }));
        };
    }

    public static class Config {
    }
}
