package ru.pricat.config.properties;

/**
 * Интерфейс для получения времени жизни access токена.
 */
public interface AccessTokenConfig {

    /**
     * Возвращает время жизни access токена.
     *
     * @return Integer с временем жизни access токена
     */
    Integer getAccessTokenMaxAge();
}
