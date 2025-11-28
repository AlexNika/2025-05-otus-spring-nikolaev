package ru.pricat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.pricat.config.properties.JwtSecretKeyConfig;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static ru.pricat.util.AppConstants.API_V1_AUTH_PATH;

/**
 * Конфигурационный класс для настройки безопасности микросервиса auth-service.
 * Определяет цепочку фильтров безопасности, настраивает аутентификацию на основе JWT,
 * определяет правила доступа к различным URL-путям и устанавливает кодировщик паролей.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {

    /**
     * Конфигурация JWT-ключа, используемая для получения секретного ключа.
     */
    private final JwtSecretKeyConfig jwtSecretKeyConfig;

    /**
     * Фильтр для проверки токенов на наличие в черном списке.
     */
    private final TokenBlacklistFilter tokenBlacklistFilter;

    /**
     * Создает цепочку фильтров безопасности для веб-приложения.
     * Настраивает разрешения для различных URL-путей, включая endpoints для регистрации и аутентификации.
     * Также настраивает OAuth2 с JWT, включает фильтр проверки черного списка токенов,
     * отключает CSRF, форму логина и HTTP Basic.
     *
     * @param http объект для настройки HTTP-безопасности
     * @return настроенная цепочка фильтров безопасности
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .addFilterBefore(tokenBlacklistFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                API_V1_AUTH_PATH + "/register",
                                API_V1_AUTH_PATH + "/login",
                                API_V1_AUTH_PATH + "/logout",
                                "/actuator/**").permitAll()
                        .pathMatchers(
                                "/favicon.ico",
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**").permitAll()
                        .pathMatchers(API_V1_AUTH_PATH + "/admin/**").hasRole("ADMIN")
                        .pathMatchers(API_V1_AUTH_PATH + "/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverter()))
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

    /**
     * Создает бин для кодирования паролей с использованием алгоритма BCrypt.
     *
     * @return кодировщик паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает реактивный декодер JWT, используя секретный ключ из конфигурации.
     * Проверяет, что ключ не пустой, иначе выбрасывает исключение.
     *
     * @return реактивный декодер JWT
     * @throws IllegalStateException если JWT-ключ не настроен
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        log.info("Initializing ReactiveJwtDecoder with shared secret");
        String jwtSecret = jwtSecretKeyConfig.getJwtSecretKey();
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("JWT secret key must be configured for Resource Server");
        }
        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }
}
