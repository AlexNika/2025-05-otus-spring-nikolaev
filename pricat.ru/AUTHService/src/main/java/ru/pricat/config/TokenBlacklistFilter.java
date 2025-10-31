package ru.pricat.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.pricat.exception.ErrorResponse;
import ru.pricat.service.TokenBlacklistService;

import java.time.Instant;

@Slf4j
@Component
public class TokenBlacklistFilter implements WebFilter {

    private final TokenBlacklistService tokenBlacklistService;

    private final ReactiveJwtDecoder jwtDecoder;

    private final ObjectMapper objectMapper;

    public TokenBlacklistFilter(TokenBlacklistService tokenBlacklistService,
                                @Lazy ReactiveJwtDecoder jwtDecoder,
                                ObjectMapper objectMapper) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtDecoder = jwtDecoder;
        this.objectMapper = objectMapper;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            return jwtDecoder.decode(token)
                    .flatMap(jwt -> {
                        String jti = jwt.getId();
                        if (tokenBlacklistService.isTokenBlacklisted(jti)) {
                            log.warn("Blocked request with blacklisted token jti: {} for path: {}", jti,
                                    exchange.getRequest().getURI());
                            return writeErrorResponse(exchange);
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(_ -> chain.filter(exchange));
        }
        return chain.filter(exchange);
    }

    /**
     * Приватный метод для формирования JSON-ответа об ошибке и записи его в тело HTTP-ответа.
     */
    private Mono<Void> writeErrorResponse(@NonNull ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Token is blacklisted",
                Instant.now()
        );

        try {
            String body = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response to JSON", e);
            return exchange.getResponse().setComplete();
        }
    }
}
