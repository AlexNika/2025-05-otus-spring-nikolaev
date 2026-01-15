package ru.pricat.service.messenger;

import ru.pricat.model.dto.events.PriceItemDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RabbitMQBatchService {
    void sendSingleItem(PriceItemDto item, String company, Instant fileProcessedAt, UUID batchId,
                        Integer totalItemsInBatch);

    void sendBatch(List<PriceItemDto> items, String company, Instant fileProcessedAt, UUID batchId);
}
