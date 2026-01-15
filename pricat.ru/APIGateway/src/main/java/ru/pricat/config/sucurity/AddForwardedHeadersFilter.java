package ru.pricat.config.sucurity;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * Настраиваемый фильтр шлюза для добавления заголовков X-Forwarded-* с использованием данных из запроса.
 * Это обеспечивает корректное построение URL-адреса в нижестоящих сервисах (например, client-service).
 */
@Component
public class AddForwardedHeadersFilter extends AbstractGatewayFilterFactory<AddForwardedHeadersFilter.Config> {

    public AddForwardedHeadersFilter() {
        super(Config.class);
    }

    /**
     * Создаёт фильтр, который добавляет заголовки X-Forwarded-Proto, X-Forwarded-Host и X-Forwarded-Port
     * на основе данных входящего запроса. Эти заголовки используются сервисами-получателями
     * для правильного формирования абсолютных URL (например, при редиректах),
     * чтобы они указывали на внешний адрес шлюза, а не на внутренний адрес микросервиса.
     *
     * @param config конфигурация фильтра (не используется)
     * @return экземпляр GatewayFilter, добавляющий X-Forwarded-* заголовки к запросу
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String scheme = request.getURI().getScheme();
            String host = request.getHeaders().getFirst("Host");
            int port = request.getURI().getPort();

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Forwarded-Proto", scheme)
                    .header("X-Forwarded-Host", host)
                    .header("X-Forwarded-Port", String.valueOf(port))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    public static class Config {
    }
}
