package ru.pricat.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Класс для хранения настроек безопасности приложения, таких как параметры JWT и refresh-токенов.
 * Обеспечивает конфигурацию через свойства application.yml с префиксом "app.security".
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class AppProperties implements InternalApiKeyConfig, JwtSecretKeyConfig, AuthServiceUrlConfig, TokensConfig {

    private String internalApiKey;

    private String authServiceInternalUrl;

    private String authServiceLbUrl;

    private Integer accessTokenMaxAge;

    private Integer refreshTokenMaxAge;

    private Integer accessTokenExpirationThreshold;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecretKey;
}
