package ru.pricat.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.config.properties.LoginAttemptsConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация {@link LoginAttemptService}, использующая in-memory кэш Caffeine
 * для отслеживания количества неудачных попыток входа по имени пользователя.
 * Пользователь считается заблокированным, если количество неудачных попыток
 * превышает заданный лимит {@code maxAttempts}.
 * После успешного входа счётчик попыток сбрасывается.
 */
@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptsConfig loginAttemptsConfig;

    /**
     * Кэш для хранения информации о попытках входа пользователей.
     * Ключ - имя пользователя, значение - {@link AttemptInfo} со счётчиком попыток.
     * Использует конфигурацию, заданную через {@code spring.cache.caffeine.spec}.
     */
    private final Cache<@NonNull String, AttemptInfo> attempts;

    public LoginAttemptServiceImpl(LoginAttemptsConfig loginAttemptsConfig) {
        this.loginAttemptsConfig = loginAttemptsConfig;
        CaffeineSpec caffeineSpec = CaffeineSpec.parse(loginAttemptsConfig.getCaffeineCacheSpec());
        this.attempts = Caffeine.from(caffeineSpec).build();
    }

    /**
     * Проверяет, заблокирован ли пользователь из-за превышения лимита неудачных попыток входа.
     * Сравнивает количество текущих попыток с {@code maxAttempts}.
     *
     * @param username имя пользователя
     * @return true, если пользователь заблокирован, иначе false
     */
    @Override
    public boolean isBlocked(String username) {
        AttemptInfo attempt = attempts.getIfPresent(username);
        if (attempt == null) {
            return false;
        }
        return attempt.getAttempts().get() >= loginAttemptsConfig.getMaxLoginAttempts();
    }

    /**
     * Регистрирует неудачную попытку входа для указанного пользователя.
     * Увеличивает счётчик попыток в {@link AttemptInfo} для этого пользователя.
     *
     * @param username имя пользователя, для которого зафиксирована неудачная попытка
     */
    @Override
    public void loginFailed(String username) {
        AttemptInfo attempt = attempts.asMap().computeIfAbsent(username, _ -> new AttemptInfo());
        attempt.getAttempts().incrementAndGet();
        log.warn("Login failed for user: {}. Attempts: {}", username, attempt.getAttempts().get());
    }

    /**
     * Регистрирует успешную попытку входа для указанного пользователя.
     * Удаляет запись о попытках из кэша, сбрасывая счётчик.
     *
     * @param username имя пользователя, для которого зафиксирован успешный вход
     */
    @Override
    public void loginSucceeded(String username) {
        attempts.invalidate(username);
        log.info("Login succeeded for user: {}, clearing attempts", username);
    }

    /**
     * Внутренний класс для хранения информации о попытках входа одного пользователя.
     * Использует {@link AtomicInteger} для потокобезопасного счёта попыток.
     */
    @Getter
    private static class AttemptInfo {
        private final AtomicInteger attempts = new AtomicInteger(0);
    }
}