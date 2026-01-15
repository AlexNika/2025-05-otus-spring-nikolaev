package ru.pricat.config.properties;

public interface JwtSecretKeyConfig {

    long getAuthTokenMaxAge();

    String getJwtSecretKey();
}
