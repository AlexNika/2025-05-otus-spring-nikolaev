package ru.pricat.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Класс для хранения настроек безопасности приложения, таких как параметры JWT и refresh-токенов.
 * Обеспечивает конфигурацию через свойства application.yml с префиксом "app.rabbitmq".
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class AppProperties implements JwtSecretKeyConfig, AuthServiceUrlConfig, AccessTokenConfig,
        RefreshTokenConfig, AccessTokenExpirationThresholdConfig {

    private String exchangeName;
    private String queueName;
    private String queueType;
    private int deliveryLimit;
    private int messageTTL;
    private String routingKey;
    private String dlExchangeName;
    private String dlQueueName;
    private String dlQueueType;
    private int dlMessageTTL;
    private String dlRoutingKey;
    private String s3EventExchangeName;
    private String s3EventQueueName;
    private String s3EventQueueType;
    private String s3EventRoutingKey;
    private boolean s3EventConsumerEnabled;
    private int retryMaxAttempts;
    private long retryInitialInterval;
    private double retryMultiplier;
    private long retryMaxInterval;
    private int batchSize;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${app.security.auth-service-lb-url}")
    private String authServiceLbUrl;

    @Value("${app.security.access-token-max-age}")
    private Integer accessTokenMaxAge;

    @Value("${app.security.refresh-token-max-age}")
    private Integer refreshTokenMaxAge;

    @Value("${app.security.access-token-expiration-threshold}")
    private Long accessTokenExpirationThreshold;
}
