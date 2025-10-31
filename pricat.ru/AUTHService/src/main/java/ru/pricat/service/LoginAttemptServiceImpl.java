package ru.pricat.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final int maxAttempts;

    private final Cache<@NonNull String, AttemptInfo> attempts;

    public LoginAttemptServiceImpl(
            @Value("${app.security.login-attempts.max}")
            int maxAttempts,
            @Value("${spring.cache.caffeine.spec}")
            String cacheSpec) {
        CaffeineSpec caffeineSpec = CaffeineSpec.parse(cacheSpec);
        log.debug("Initial CaffeineSpec (from application.yml): {}", caffeineSpec);
        this.maxAttempts = maxAttempts;
        this.attempts = Caffeine.from(caffeineSpec).build();
    }

    @Override
    public boolean isBlocked(String username) {
        AttemptInfo attempt = attempts.getIfPresent(username);
        if (attempt == null) {
            return false;
        }
        return attempt.getAttempts().get() >= maxAttempts;
    }

    @Override
    public void loginFailed(String username) {
        AttemptInfo attempt = attempts.asMap().computeIfAbsent(username, _ -> new AttemptInfo());
        attempt.getAttempts().incrementAndGet();
        log.warn("Login failed for user: {}. Attempts: {}", username, attempt.getAttempts().get());
    }

    @Override
    public void loginSucceeded(String username) {
        attempts.invalidate(username);
        log.info("Login succeeded for user: {}, clearing attempts", username);
    }

    @Getter
    private static class AttemptInfo {
        private final AtomicInteger attempts = new AtomicInteger(0);
    }
}