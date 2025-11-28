package ru.pricat.config.sucurity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import ru.pricat.config.properties.JwtSecretKeyConfig;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ru.pricat.util.AppConstants.API_V1_AUTH_PATH;
import static ru.pricat.util.AppConstants.API_V1_CLIENT_PATH;
import static ru.pricat.util.AppConstants.API_V1_SEARCH_PATH;

/**
 * Конфигурационный класс для настройки безопасности веб-приложения.
 * Определяет цепочку фильтров безопасности, декодер JWT и настройки CORS.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtSecretKeyConfig jwtSecretKeyConfig;

    /**
     * Создает цепочку фильтров безопасности для веб-приложения.
     * Настраивает разрешения для различных URL-путей, включая endpoints для аутентификации и администраторов.
     * Также настраивает OAuth2 с JWT, отключает CSRF, форму логина и HTTP Basic,
     * а также включает CORS с заданной конфигурацией.
     *
     * @param http объект для настройки HTTP-безопасности
     * @return настроенная цепочка фильтров безопасности
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configuring SecurityWebFilterChain for API Gateway");

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                API_V1_AUTH_PATH + "/login",
                                API_V1_AUTH_PATH + "/register",
                                API_V1_AUTH_PATH + "/refresh",
                                API_V1_AUTH_PATH + "/logout"
                        ).permitAll()
                        .pathMatchers(
                                API_V1_AUTH_PATH + "/user/*/add-role",
                                API_V1_AUTH_PATH + "/user/*/remove-role",
                                API_V1_AUTH_PATH + "/user/*/change-password",
                                API_V1_AUTH_PATH + "/user/*"
                        ).hasRole("ADMIN")
                        .pathMatchers(API_V1_AUTH_PATH + "/admin/**").hasRole("ADMIN")
                        .pathMatchers(API_V1_AUTH_PATH + "/**").authenticated()
                        .pathMatchers(
                                "/webjars/**",
                                "/js/**",
                                "/css/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                        .pathMatchers("/login",
                                "/register",
                                "/profile",
                                "/profile-edit",
                                "/clients",
                                "/files",
                                "/search",
                                "/search/**",
                                "/admin",
                                "/user/**").permitAll()
                        .pathMatchers(
                                API_V1_CLIENT_PATH + "/**",
                                API_V1_CLIENT_PATH + "/debug-headers").authenticated()
                        .pathMatchers(
                                API_V1_SEARCH_PATH + "/**").authenticated()
                        .pathMatchers(
                                "/actuator/**",
                                "/gateway/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
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
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    /**
     * Создает источник конфигурации CORS с разрешенными источниками, методами и заголовками.
     * Включает поддержку учетных данных для CORS-запросов.
     *
     * @return источник конфигурации CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:[*]", "http://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
