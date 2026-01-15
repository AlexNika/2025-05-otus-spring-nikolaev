package ru.pricat.config.properties;

public interface RefreshTokenConfig {

    long getRefreshTokenMaxAge();

    String getRefreshTokenCleanupTime();
}
