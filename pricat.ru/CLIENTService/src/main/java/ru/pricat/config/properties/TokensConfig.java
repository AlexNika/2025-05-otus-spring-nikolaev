package ru.pricat.config.properties;

public interface TokensConfig {

    /**
     * Возвращает время жизни access токена.
     *
     * @return Long с временем жизни access токена
     */
    Integer getAccessTokenMaxAge();

    /**
     * Возвращает время жизни refresh токена.
     *
     * @return Long с временем жизни refresh токена
     */
    Integer getRefreshTokenMaxAge();

    /**
     * Возвращает порог истечения срока действия access токена.
     *
     * @return Integer с временем жизни access токена
     */
    Integer getAccessTokenExpirationThreshold();
}
