package ru.pricat.config.properties;

/**
 * Интерфейс для получения порога истечения срока действия access токена.
 */
public interface AccessTokenExpirationThresholdConfig {

    /**
     * Возвращает порог истечения срока действия access токена.
     *
     * @return Integer с временем жизни access токена
     */
    Long getAccessTokenExpirationThreshold();
}
