package ru.pricat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {
    private String exchangeName;
    private String queueName;
    private String queueType;
    private int deliveryLimit;
    private int messageTTL;
    private String routingKey;
    private String dlExchangeName;
    private String dlQueueName;
    private String dlQueueType;
    private int dlMessageTTL;
    private String dlRoutingKey;
    private String s3EventExchangeName;
    private String s3EventQueueName;
    private String s3EventQueueType;
    private String s3EventRoutingKey;
    private boolean s3EventConsumerEnabled;
    private int retryMaxAttempts;
    private long retryInitialInterval;
    private double retryMultiplier;
    private long retryMaxInterval;
    private int batchSize;
}
