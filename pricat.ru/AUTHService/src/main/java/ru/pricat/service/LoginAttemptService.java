package ru.pricat.service;

public interface LoginAttemptService {
    boolean isBlocked(String username);

    void loginFailed(String username);

    void loginSucceeded(String username);
}
