package ru.pricat.config.sucurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Filter to handle the response from /auth/logout.
 * It clears the httpOnly refreshToken cookie regardless of the logout outcome.
 */
@Slf4j
@Component
public class ClearRefreshTokenCookieFilter extends AbstractGatewayFilterFactory<ClearRefreshTokenCookieFilter.Config> {

    public ClearRefreshTokenCookieFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            if (exchange.getRequest().getURI().getPath().equals("/api/v1/auth/logout")) {
                log.debug("Clearing refreshToken cookie on logout");
                ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();
                response.addCookie(deleteCookie);
            }
        }));
    }

    public static class Config {
    }
}
