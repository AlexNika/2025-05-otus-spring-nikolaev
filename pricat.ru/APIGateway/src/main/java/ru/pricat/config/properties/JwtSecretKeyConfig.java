package ru.pricat.config.properties;

/**
 * Интерфейс для получения время жизни refresh-токена в миллисекундах.
 */
public interface JwtSecretKeyConfig {

    /**
     * Возвращает секретный ключ для подписи JWT-токенов.
     *
     * @return строка с секретным ключом
     */
    String getJwtSecretKey();
}
