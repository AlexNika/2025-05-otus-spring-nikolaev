package ru.pricat.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.pricat.config.properties.RefreshTokenConfig;
import ru.pricat.config.sucurity.HandleRefreshTokenFilter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleRefreshTokenFilterTest {

    @Spy
    private RefreshTokenConfig refreshTokenConfig;

    @Spy
    @InjectMocks
    private HandleRefreshTokenFilter filter;

    @Captor
    private ArgumentCaptor<ServerWebExchange> exchangeCaptor;

    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        lenient().when(refreshTokenConfig.getRefreshTokenMaxAge()).thenReturn(Duration.ofDays(30).getSeconds());
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    void shouldAddRefreshTokenToHeaderWhenCookiePresentAndValid() {
        String token = "valid-refresh-Token-1234567890";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/refresh")
                .cookie(new HttpCookie("refreshToken", token))
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // ✅ Устанавливаем атрибут, который фильтр ожидает
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR,
                Collections.singleton(request.getURI()));

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.apply(new HandleRefreshTokenFilter.Config()).filter(exchange, chain).block();

        verify(chain).filter(exchangeCaptor.capture());
        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        String headerValue = capturedExchange.getRequest().getHeaders().getFirst("X-Refresh-Token");
        assertEquals(token, headerValue);
    }

    @Test
    void shouldRemoveXRefreshTokenHeaderAlways() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/refresh")
                .cookie(new HttpCookie("refreshToken", "valid"))
                .build();

        MockServerHttpResponse response = new MockServerHttpResponse();
        response.getHeaders().add("X-Refresh-Token", "new123");

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().getHeaders().addAll(response.getHeaders());

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR,
                Collections.singleton(request.getURI()));

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.apply(new HandleRefreshTokenFilter.Config()).filter(exchange, chain).block();

        assertNull(exchange.getResponse().getHeaders().getFirst("X-Refresh-Token"));
    }

    @Test
    void shouldNotSetCookieWhenResponseStatusNot2xx() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/refresh")
                .cookie(new HttpCookie("refreshToken", "old"))
                .build();

        MockServerHttpResponse response = new MockServerHttpResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("X-Refresh-Token", "new123");

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().getHeaders().addAll(response.getHeaders());
        exchange.getResponse().setStatusCode(response.getStatusCode());

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR,
                Collections.singleton(request.getURI()));

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.apply(new HandleRefreshTokenFilter.Config()).filter(exchange, chain).block();

        List<ResponseCookie> cookies = exchange.getResponse().getCookies().get("refreshToken");
        assertNull(cookies);
    }
}