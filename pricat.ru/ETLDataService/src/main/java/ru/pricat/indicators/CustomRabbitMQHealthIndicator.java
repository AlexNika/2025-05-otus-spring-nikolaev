package ru.pricat.indicators;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.pricat.config.RabbitMQProperties;

import java.util.HashMap;
import java.util.Map;


/**
 * Кастомный Health индикатор для RabbitMQ
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomRabbitMQHealthIndicator implements HealthIndicator {

    final RabbitTemplate rabbitTemplate;

    final String exchangeName;

    final String s3EventExchangeName;

    public CustomRabbitMQHealthIndicator(RabbitTemplate rabbitTemplate, RabbitMQProperties rabbitMQProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = rabbitMQProperties.getExchangeName();
        this.s3EventExchangeName = rabbitMQProperties.getS3EventExchangeName();
    }


    /**
     * Метод переопределяет стандартный health метод с внедренным кастомным индикатором для RabbitMQ
     *
     * @return - переопределенный Health объект
     */
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(exchangeName);
                details.put("mainExchange", "AVAILABLE");
                channel.exchangeDeclarePassive(s3EventExchangeName);
                details.put("s3EventExchange", "AVAILABLE");
                return null;
            });
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    "health.check",
                    "RabbitMQ Health Test"
            );
            details.put("messageSending", "OPERATIONAL");
            return Health.up()
                    .withDetails(details)
                    .build();
        } catch (Exception e) {
            log.warn("RabbitMQ health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetails(details)
                    .withException(e)
                    .build();
        }
    }
}
