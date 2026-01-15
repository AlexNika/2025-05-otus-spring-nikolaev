package ru.pricat.config.sucurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * Фильтр для логирования входящих HTTP-запросов и информации о маршрутизации.
 * Записывает в лог исходный URL, идентификатор маршрута и конечный URI.
 * Используется как глобальный фильтр в Spring Cloud Gateway.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter {

    /**
     * Метод фильтрации, вызываемый при обработке каждого входящего запроса.
     * Извлекает исходный URL, маршрут и целевой URI, затем записывает эту информацию в лог.
     * После логирования передаёт запрос дальше по цепочке фильтров.
     *
     * @param exchange текущий серверный веб-обмен (запрос и ответ)
     * @param chain цепочка фильтров, через которую продолжается обработка запроса
     * @return Mono<Void>, сигнализирующий о завершении обработки
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Set<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, Collections.emptySet());
        String originalUri = (uris.isEmpty()) ? "Unknown" : uris.iterator().next().toString();
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        URI routeUri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        log.info("Incoming request {} is routed to id: {}, uri:{}", originalUri, Objects.requireNonNull(route).getId(),
                routeUri);
        return chain.filter(exchange);
    }
}
