package ru.pricat.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.config.properties.LoginAttemptsConfig;

import java.time.Duration;

/**
 * Реализация {@link TokenBlacklistService}, использующая in-memory кэш Caffeine
 * для хранения идентификаторов (jti) токенов, добавленных в черный список.
 * Кэш настраивается с максимальным размером и временем жизни элементов (TTL),
 * значения которых берутся из {@link LoginAttemptsConfig}.
 */
@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    /**
     * Кэш для хранения jti токенов, добавленных в черный список.
     * Ключ - jti токена, значение - произвольное значение (true), используется для проверки наличия.
     * Автоматически очищает устаревшие записи по истечении TTL.
     */
    private final Cache<@NonNull String, Boolean> blacklistedTokens;

    /**
     * Конструктор, инициализирующий кэш с настройками из конфигурации.
     *
     * @param loginAttemptsConfig конфигурация, содержащая параметры для кэша черного списка
     *                            (максимальный размер и время жизни)
     */
    public TokenBlacklistServiceImpl(
            LoginAttemptsConfig loginAttemptsConfig) {
        this.blacklistedTokens = Caffeine.newBuilder()
                .maximumSize(loginAttemptsConfig.getTokenBlacklistMaxSize())
                .expireAfterWrite(Duration.ofMinutes(loginAttemptsConfig.getTokenBlacklistTtlMinutes()))
                .build();
    }

    /**
     * Добавляет JWT-токен в черный список по его идентификатору (jti).
     * Токен будет считаться недействительным до истечения срока его хранения в кэше.
     *
     * @param tokenJti уникальный идентификатор (jti) токена
     */
    @Override
    public void blacklistToken(String tokenJti) {
        blacklistedTokens.put(tokenJti, true);
        log.info("Token with jti '{}' has been blacklisted.", tokenJti);
    }

    /**
     * Проверяет, находится ли токен с указанным jti в черном списке.
     *
     * @param tokenJti уникальный идентификатор (jti) токена для проверки
     * @return true, если токен находится в черном списке, иначе false
     */
    @Override
    public boolean isTokenBlacklisted(String tokenJti) {
        boolean isBlacklisted = blacklistedTokens.getIfPresent(tokenJti) != null;
        if (isBlacklisted) {
            log.info("Token with jti '{}' found in blacklist.", tokenJti);
        }
        return isBlacklisted;
    }

}
