package ru.pricat.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.pricat.model.dto.s3.S3EventDto;
import ru.pricat.service.queue.EventQueueService;
import ru.pricat.service.s3.S3EventService;

import java.nio.charset.StandardCharsets;

/**
 * Консьюмер для обработки S3 событий из RabbitMQ.
 * Он слушает очередь s3minio.event.queue и сохраняет события в БД
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class S3EventConsumer {

    private final S3EventService s3EventService;

    private final EventQueueService eventQueueService;

    /**
     * Метод прослушивает очередь RabbitMQ и обрабатывает объект Message полученный из нее.
     * Если message существует и валидный, то message body записывается в базу данных
     * и добавляется в очередь для дальнейшей обработки.
     *
     * @param message Message объект полученный из прослушиваемой очереди RabbitMQ
     */
    @RabbitListener(
            queues = "#{@rabbitMQProperties.getS3EventQueueName}",
            autoStartup = "#{@rabbitMQProperties.isS3EventConsumerEnabled}",
            ackMode = "AUTO"
    )
    public void handleS3Event(Message message) {
        String jsonEvent = null;
        try {
            jsonEvent = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("Получено S3 событие из RabbitMQ. Длина сообщения: {} символов", jsonEvent.length());
            S3EventDto savedEvent = s3EventService.saveEventFromJson(jsonEvent);
            if (savedEvent.objectKey().contains("health-check")) {
                log.info("Health check event handled. No other action needed!");
                return;
            }
            boolean addedToQueue = eventQueueService.addEvent(savedEvent);
            if (addedToQueue) {
                log.info("S3 событие успешно сохранено в БД. ID: {}, Тип: {}, Бакет: {}, Ключ: {}",
                        savedEvent.id(),
                        savedEvent.eventType(),
                        savedEvent.bucketName(),
                        savedEvent.objectKey());
            } else {
                log.warn("Событие сохранено в БД, но не добавлено в очередь (возможный дубликат). ID: {}",
                        savedEvent.id());
            }
        } catch (Exception e) {
            log.error("Ошибка обработки S3 события из RabbitMQ. Длина сообщения: {} символов",
                    jsonEvent != null ? jsonEvent.length() : 0, e);
            if (jsonEvent != null) {
                log.debug("Содержимое ошибочного сообщения (первые 500 символов): {}",
                        jsonEvent.substring(0, Math.min(jsonEvent.length(), 500)));
            }
            throw new AmqpRejectAndDontRequeueException("Ошибка обработки S3 события: " + e.getMessage(), e);
        }
    }
}
