package ru.pricat.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pricat.model.PriceItemMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class BatchAggregatorServiceImpl implements BatchAggregatorService {

    private final Map<UUID, AggregationContext> aggregationContexts = new ConcurrentHashMap<>();

    private final PriceListProcessingService pricelistProcessingService;

    @Override
    public void addMessage(PriceItemMessage message) {
        if (!message.isValid()) {
            log.warn("Пропущено невалидное сообщение: messageId={}", message.messageId());
            return;
        }
        UUID batchId = message.batchId();
        AggregationContext context = aggregationContexts.computeIfAbsent(
                batchId,
                _ -> new AggregationContext(message.totalItemsInBatch(), message.company())
        );
        synchronized (context) {
            context.addMessage(message);
            log.debug("Батч {}: получено {}/{} сообщений", batchId, context.getReceivedCount(), context.getTotalCount());

            if (context.isComplete()) {
                aggregationContexts.remove(batchId);
                log.info("Батч {} завершён. Запуск обработки...", batchId);
                pricelistProcessingService.processPriceList(context.getCompany(), batchId, context.getMessages());
            }
        }
    }

    @Getter
    private static class AggregationContext {

        private final int totalCount;

        private final String company;

        private final List<PriceItemMessage> messages = new CopyOnWriteArrayList<>();

        public AggregationContext(int totalCount, String company) {
            this.totalCount = totalCount;
            this.company = company;
        }

        public synchronized void addMessage(PriceItemMessage message) {
            messages.add(message);
        }

        public boolean isComplete() {
            return messages.size() >= totalCount;
        }

        public int getReceivedCount() {
            return messages.size();
        }
    }
}
