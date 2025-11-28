package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.pricat.repository.UserRefreshTokenRepository;

/**
 * Реализация {@link RefreshTokenCleanupService} для очистки просроченных refresh-токенов.
 * Использует {@link UserRefreshTokenRepository} для выполнения операций удаления.
 * Содержит метод, запускаемый по расписанию, и асинхронный метод для ручного вызова.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupServiceImpl implements RefreshTokenCleanupService {

    /**
     * Репозиторий для работы с сущностями refresh-токенов пользователей.
     */
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    /**
     * Удаляет просроченные refresh-токены из хранилища.
     * Метод запускается автоматически по расписанию (каждый день в 02:00 утра).
     * Использует cron-выражение "0 0 2 * * ?".
     * Операция выполняется синхронно с помощью {@link Mono#block()}.
     */
    @Scheduled(cron = "#{@appProperties.getRefreshTokenCleanupTime()}")
    @Override
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens...");
        userRefreshTokenRepository.deleteExpiredTokens().block();
        log.info("Scheduled cleanup of expired refresh tokens completed.");
    }

    /**
     * Асинхронно удаляет просроченные refresh-токены из хранилища.
     * Выполняет операцию через репозиторий и логирует результат или ошибку.
     *
     * @return реактивный объект, сигнализирующий о завершении операции
     */
    @Override
    public Mono<Void> cleanupExpiredTokensAsync() {
        log.info("Starting async cleanup of expired refresh tokens...");
        return userRefreshTokenRepository.deleteExpiredTokens()
                .doOnSuccess(_ -> log.info("Async cleanup of expired refresh tokens completed."))
                .doOnError(error -> log.error("Error during async cleanup of expired refresh tokens", error));
    }
}
