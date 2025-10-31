package ru.pricat.service;

public interface TokenBlacklistService {
    void blacklistToken(String tokenJti);

    boolean isTokenBlacklisted(String tokenJti);
}
