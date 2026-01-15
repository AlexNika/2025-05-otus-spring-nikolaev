package ru.pricat.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import ru.pricat.config.properties.JwtSecretKeyConfig;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Конфигурационный класс для настройки JWT декодера.
 * Создает бин JwtDecoder, используя общий секретный ключ.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtSecretKeyConfig jwtSecretKeyConfig;

    /**
     * Создает JWT декодер, используя HMAC с SHA-256.
     * Используется для проверки подписи JWT токена.
     *
     * @return настроенный JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwtSecretKey = jwtSecretKeyConfig.getJwtSecretKey();
        log.debug("JWT Secret Key: {}", jwtSecretKey);
        SecretKey secretKey = new SecretKeySpec(jwtSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
