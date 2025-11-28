package ru.pricat.model;

import ru.pricat.model.dto.PriceItemDto;

import java.time.Instant;
import java.util.UUID;

/**
 * Сообщение из RabbitMQ с данными о цене товара.
 */
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
    /**
     * Проверяет валидность сообщения.
     */
    public boolean isValid() {
        return company != null && priceItem != null && batchId != null;
    }
}
