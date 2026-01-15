package ru.pricat.model.events;

import lombok.Builder;
import ru.pricat.model.dto.events.PriceItemDto;

import java.time.Instant;
import java.util.UUID;

/**
 * Сообщение для RabbitMQ с данными о цене и остатке товара.
 */
@Builder
@SuppressWarnings("unused")
public record PriceItemMessage(
        UUID messageId,
        UUID batchId,
        Integer totalItemsInBatch,
        UUID itemId,
        String company,
        Instant fileProcessedAt,
        Instant messageSentAt,
        PriceItemDto priceItem
) {
    public PriceItemMessage {
        if (messageId == null) {
            messageId = UUID.randomUUID();
        }
        if (messageSentAt == null) {
            messageSentAt = Instant.now();
        }
    }

    /**
     * Генерирует ключ для проверки дубликатов.
     */
    public String getIdempotencyKey() {
        if (isValid()) {
            return String.format("%s:%s:%s", company, priceItem.getProductId(), batchId);
        }
        return null;
    }

    /**
     * Проверяет валидность сообщения.
     */
    public boolean isValid() {
        return company != null && priceItem != null && batchId != null;
    }
}
