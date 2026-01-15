package ru.pricat.service.messenger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.pricat.config.RabbitMQProperties;
import ru.pricat.model.dto.events.PriceItemDto;
import ru.pricat.model.events.PriceItemMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для батчевой отправки сообщений в RabbitMQ.
 */
@Slf4j
@Service
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class RabbitMQBatchServiceImpl implements RabbitMQBatchService {

    private final RabbitTemplate rabbitTemplate;

    private final String exchangeName;

    private final String routingKey;

    private final int batchSize;

    public RabbitMQBatchServiceImpl(RabbitTemplate rabbitTemplate, RabbitMQProperties rabbitMQProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = rabbitMQProperties.getExchangeName();
        this.routingKey = rabbitMQProperties.getRoutingKey();
        this.batchSize = rabbitMQProperties.getBatchSize();
    }

    /**
     * Отправляет один товар в RabbitMQ с Retry
     */
    @Override
    @Retryable(
            retryFor = {
                    AmqpException.class,
                    org.springframework.amqp.AmqpConnectException.class,
                    java.net.ConnectException.class
            },
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendSingleItem(PriceItemDto item, String company, Instant fileProcessedAt, UUID batchId,
                               Integer totalItemsInBatch) {
        try {
            PriceItemMessage message = PriceItemMessage.builder()
                    .priceItem(item)
                    .company(company)
                    .fileProcessedAt(fileProcessedAt)
                    .batchId(batchId)
                    .totalItemsInBatch(totalItemsInBatch)
                    .itemId(item.getItemId())
                    .build();
            String finalRoutingKey = generateRoutingKey(company);
            CorrelationData correlationData = new CorrelationData(
                    String.format("%s-%s-%s", company, item.getProductId(), UUID.randomUUID())
            );
            rabbitTemplate.convertAndSend(exchangeName, finalRoutingKey, message,
                    correlationData);
            log.debug("Sent message for product {} from company {}", item.getProductId(), company);

        } catch (Exception e) {
            log.error("Failed to send message for product {}: {}", item.getProductId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Fallback метод когда все retry attempts исчерпаны
     */
    @Recover
    public void sendSingleItemFallback(AmqpException e, PriceItemDto item, String company,
                                       Instant fileProcessedAt, UUID batchId, Integer totalItemsInBatch) {
        log.error("FAILED to send message for product {} after all retry attempts. Company: {}, Error: {}",
                item.getProductId(), company, e.getMessage());
        throw new RuntimeException("Все retry попытки исчерпаны: " + e.getMessage(), e);
    }

    /**
     * Отправляет батч товаров в RabbitMQ с улучшенной обработкой ошибок.
     */
    @Override
    public void sendBatch(List<PriceItemDto> items, String company, Instant fileProcessedAt, UUID batchId) {
        if (items == null || items.isEmpty()) {
            log.warn("Attempted to send empty batch for company {}", company);
            return;
        }
        int totalItems = items.size();
        int batches = (int) Math.ceil((double) totalItems / batchSize);
        int successfulItems = 0;
        int failedItems = 0;
        log.info("Sending {} items in {} batches for company {}", totalItems, batches, company);
        for (int i = 0; i < batches; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, totalItems);
            List<PriceItemDto> batch = items.subList(fromIndex, toIndex);
            BatchSendResult result = sendSingleBatch(batch, company, fileProcessedAt, batchId, i + 1,
                    batches, totalItems);
            successfulItems += result.successful();
            failedItems += result.failed();
        }
        if (failedItems > 0) {
            log.warn("Batch send completed with failures - Company: {}, Successful: {}, Failed: {}",
                    company, successfulItems, failedItems);
        } else {
            log.info("Successfully sent all {} items for company {}", totalItems, company);
        }
    }

    /**
     * Отправляет один батч сообщений с обработкой ошибок на уровне item
     */
    private BatchSendResult sendSingleBatch(List<PriceItemDto> batch, String company,
                                            Instant fileProcessedAt, UUID batchId,
                                            int batchNumber, int totalBatches, int totalItems) {
        int successful = 0;
        int failed = 0;
        for (PriceItemDto item : batch) {
            try {
                sendSingleItem(item, company, fileProcessedAt, batchId, totalItems);
                successful++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to send item {} in batch {}/{}: {}",
                        item.getProductId(), batchNumber, totalBatches, e.getMessage());
            }
        }
        if (failed > 0) {
            log.warn("Batch {}/{} completed with failures - Successful: {}, Failed: {}",
                    batchNumber, totalBatches, successful, failed);
        } else {
            log.debug("Sent batch {}/{} for company {} ({} items)",
                    batchNumber, totalBatches, company, batch.size());
        }
        return new BatchSendResult(successful, failed);
    }

    /**
     * Результат отправки батча
     */
    private record BatchSendResult(int successful, int failed) {
    }

    /**
     * Генерирует routing key на основе компании.
     */
    private String generateRoutingKey(String company) {
        return routingKey.substring(0, routingKey.length() - 1)
               + company.toLowerCase();
    }
}
