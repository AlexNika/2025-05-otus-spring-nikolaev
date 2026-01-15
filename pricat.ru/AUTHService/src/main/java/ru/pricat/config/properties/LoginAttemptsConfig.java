package ru.pricat.config.properties;

public interface LoginAttemptsConfig {

    int getMaxLoginAttempts();

    String getCaffeineCacheSpec();

    long getTokenBlacklistTtlMinutes();

    long getTokenBlacklistMaxSize();
}
