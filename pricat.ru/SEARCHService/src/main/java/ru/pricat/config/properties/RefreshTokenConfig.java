package ru.pricat.config.properties;

/**
 * Интерфейс для получения времени жизни refresh токена.
 */
public interface RefreshTokenConfig {

    /**
     * Возвращает время жизни refresh токена.
     *
     * @return Integer с временем жизни refresh токена
     */
    Integer getRefreshTokenMaxAge();
}
