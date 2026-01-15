package ru.pricat.service;

import reactor.core.publisher.Mono;

/**
 * Интерфейс сервиса для очистки просроченных refresh-токенов из хранилища.
 * Предоставляет как синхронный метод, запускаемый по расписанию, так и асинхронный метод.
 */
public interface RefreshTokenCleanupService {

    /**
     * Удаляет просроченные refresh-токены из хранилища.
     * Метод запускается автоматически по расписанию (каждый день в 02:00 утра).
     * Использует cron-выражение "0 0 2 * * ?".
     */
    void cleanupExpiredTokens();

    /**
     * Асинхронно удаляет просроченные refresh-токены из хранилища.
     *
     * @return реактивный объект, сигнализирующий о завершении операции
     */
    Mono<Void> cleanupExpiredTokensAsync();
}
