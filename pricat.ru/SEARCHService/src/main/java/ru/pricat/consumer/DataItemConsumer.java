package ru.pricat.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.pricat.model.PriceItemMessage;
import ru.pricat.service.BatchAggregatorService;

/**
 * Консьюмер для обработки полезной нагрузки - data items из RabbitMQ.
 * Он слушает очередь, получает messages и отправляет их на дальнейшуу обработку
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("SpellCheckingInspection")
public class DataItemConsumer {

    final BatchAggregatorService batchAggregatorService;

    @RabbitListener(queues = "#{@appProperties.getQueueName}",
            ackMode = "AUTO")
    public void handleDataPayload(PriceItemMessage message) {
        log.debug("Получено сообщение: batchId={}, company={}",
                message.batchId(), message.company());
        batchAggregatorService.addMessage(message);
    }
}
