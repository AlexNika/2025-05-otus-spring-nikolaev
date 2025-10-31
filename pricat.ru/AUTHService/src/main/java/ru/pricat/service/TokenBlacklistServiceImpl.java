package ru.pricat.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Cache<@NonNull String, Boolean> blacklistedTokens;

    public TokenBlacklistServiceImpl(
            @Value("${app.security.token-blacklist.ttl-minutes}")
            long ttlMinutes) {
        this.blacklistedTokens = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .build();
    }

    @Override
    public void blacklistToken(String tokenJti) {
        blacklistedTokens.put(tokenJti, true);
        log.info("Token with jti '{}' has been blacklisted.", tokenJti);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenJti) {
        boolean isBlacklisted = blacklistedTokens.getIfPresent(tokenJti) != null;
        if (isBlacklisted) {
            log.debug("Token with jti '{}' found in blacklist.", tokenJti);
        }
        return isBlacklisted;
    }

}
