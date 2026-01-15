package ru.pricat.service.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.model.dto.s3.S3EventDto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для управления in-memory очередью событий
 * использует LinkedBlockingQueue для thread-safe операций
 */
@Slf4j
@Service
@SuppressWarnings("SpellCheckingInspection")
public class EventQueueServiceImpl implements EventQueueService {

    private final BlockingQueue<S3EventDto> eventQueue = new LinkedBlockingQueue<>();

    private final ConcurrentHashMap<String, Long> eventSignatureCache = new ConcurrentHashMap<>();

    private volatile boolean isActive = true;

    private static final long SIGNATURE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

    /**
     * Добавляет событие в очередь с проверкой дубликатов
     */
    @Override
    public boolean addEvent(S3EventDto event) {
        if (!isActive || event == null) {
            return false;
        }
        if (isDuplicateEvent(event)) {
            log.info("Duplicate event skipped - bucket: {}, key: {}, eTag: {}",
                    event.bucketName(), event.objectKey(), event.objectETag());
            return false;
        }
        try {
            eventQueue.put(event);

            String signature = generateEventSignature(event);
            eventSignatureCache.put(signature, System.currentTimeMillis());

            log.debug("Event added to queue: {} (bucket: {}, key: {})",
                    event.id(), event.bucketName(), event.objectKey());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while adding event to queue: {}", event.id(), e);
            return false;
        }
    }

    /**
     * Удаляет подпись события из кэша (после успешной обработки)
     */
    @Override
    public void removeEventSignature(S3EventDto eventDto) {
        String signature = generateEventSignature(eventDto);
        eventSignatureCache.remove(signature);
        log.debug("Event signature removed from cache: {}", signature);
    }

    /**
     * Очищает устаревшие подписи из кэша
     */
    @Override
    public void cleanupExpiredSignatures() {
        long currentTime = System.currentTimeMillis();
        eventSignatureCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > SIGNATURE_TTL_MS
        );
        log.debug("Expired signatures cleaned up");
    }

    /**
     * Блокирующее получение события из очереди
     */
    @Override
    public S3EventDto takeEvent() throws InterruptedException {
        return eventQueue.take();
    }

    /**
     * Немедленное получение события (без блокировки)
     */
    @Override
    public S3EventDto pollEvent() {
        return eventQueue.poll();
    }

    /**
     * Возвращает размер очереди
     */
    @Override
    public int getQueueSize() {
        return eventQueue.size();
    }

    /**
     * Проверяет, пуста ли очередь
     */
    @Override
    public boolean isEmpty() {
        return eventQueue.isEmpty();
    }

    /**
     * Останавливает обработку новых событий
     */
    @Override
    public void stop() {
        isActive = false;
        log.info("Event queue service stopped");
    }

    /**
     * Возобновляет обработку событий
     */
    @Override
    public void start() {
        isActive = true;
        log.info("Event queue service started");
    }

    /**
     * Генерирует уникальную подпись для дедубликации
     */
    private String generateEventSignature(S3EventDto event) {
        if (event.eventType().toString().contains("ObjectRemoved")) {
            return "DELETE_" + event.id() + "_" + System.currentTimeMillis();
        }
        return String.format("%s:%s:%s:%s",
                event.bucketName(),
                event.objectKey(),
                event.objectETag() != null ? event.objectETag() : "null",
                event.objectSize() != null ? event.objectSize() : "null"
        );
    }

    /**
     * Проверяет, является ли событие дубликатом
     */
    private boolean isDuplicateEvent(S3EventDto event) {
        String signature = generateEventSignature(event);
        Long timestamp = eventSignatureCache.get(signature);

        if (timestamp != null) {
            if (System.currentTimeMillis() - timestamp < SIGNATURE_TTL_MS) {
                log.debug("Duplicate event detected and skipped: {}", signature);
                return true;
            } else {
                eventSignatureCache.remove(signature);
            }
        }
        return false;
    }
}
