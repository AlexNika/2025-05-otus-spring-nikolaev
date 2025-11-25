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
public class AppProperties implements JwtSecretKeyConfig, AccessTokenConfig, RefreshTokenConfig {

    /**
     * Максимальное время жизни access-токена в миллисекундах.
     * Значение устанавливается из конфигурационного файла.
     */
    private Long accessTokenMaxAge;

    /**
     * Максимальное время жизни refresh-токена в миллисекундах.
     * Значение устанавливается из конфигурационного файла.
     */
    private Long refreshTokenMaxAge;

    /**
     * Секретный ключ для подписи JWT-токенов.
     * Значение устанавливается из свойства "spring.security.oauth2.resourceserver.jwt.secret-key".
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecretKey;
}
