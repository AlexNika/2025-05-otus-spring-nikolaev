package ru.pricat.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Класс для хранения настроек приложения.
 * Обеспечивает конфигурацию через свойства application.yml с префиксом "app.security",
 * а также другие настройки, разбросанные по разным частям файла конфигурации
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class AppProperties implements InternalApiKeyConfig, JwtSecretKeyConfig, RefreshTokenConfig,
        LoginAttemptsConfig {

    private String internalApiKey;

    private long authTokenMaxAge;

    private long refreshTokenMaxAge;

    private String refreshTokenCleanupTime;

    private int maxLoginAttempts;

    private long tokenBlacklistTtlMinutes;

    private long tokenBlacklistMaxSize;

    private String clientServiceBaseUrl;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${spring.cache.caffeine.spec}")
    private String caffeineCacheSpec;
}
