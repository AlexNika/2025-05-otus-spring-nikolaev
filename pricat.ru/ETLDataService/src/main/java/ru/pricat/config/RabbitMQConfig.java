package ru.pricat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.util.ErrorHandler;
import ru.pricat.exception.CustomExceptionStrategy;

/**
 * Конфигурация для RabbitMQ.
 */
@Slf4j
@EnableRetry
@EnableRabbit
@Configuration
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;

    public RabbitMQConfig(RabbitMQProperties rabbitMQProperties) {
        this.rabbitMQProperties = rabbitMQProperties;
    }

    /**
     * Метод создает Direct Exchange для событий, публикуемых из S3 хранилища.
     *
     * @return DirectExchange с durable=true, autoDelete=false
     */
    @Bean
    public DirectExchange s3EventExchange() {
        return new DirectExchange(rabbitMQProperties.getS3EventExchangeName(), true, false);
    }

    /**
     * Метод создает Topic Exchange для ценовых обновлений.
     *
     * <p>Topic Exchange позволяет гибкую маршрутизацию на основе routing key.
     * Например, можно подписываться на цены конкретной компании или категории.</p>
     *
     * @return TopicExchange с durable=true, autoDelete=false
     */
    @Bean
    public TopicExchange dataExchange() {
        return new TopicExchange(rabbitMQProperties.getExchangeName(), true, false);
    }

    /**
     * Метод создает Dead Letter Exchange для обработки неудачных сообщений.
     */
    @Bean
    public DirectExchange dlDataExchange() {
        return new DirectExchange(rabbitMQProperties.getDlExchangeName(), true, false);
    }

    /**
     * Метод создает durable Classic Queue для хранения событий, публикуемых из S3 хранилища.
     */
    @Bean
    public Queue s3EventQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getS3EventQueueName())
                .withArgument("x-queue-type", rabbitMQProperties.getS3EventQueueType())
                .build();
    }

    /**
     * Метод создает durable очередь с quorum replication для отказоустойчивости.
     *
     * <p>Quorum очередь обеспечивает репликацию сообщений между узлами RabbitMQ
     * для повышения надежности в кластерной конфигурации.</p>
     * <ul>
     *   <li>Quorum queues: готовы к кластеризации (работают и в single-node)</li>
     *   <li>DLX: перенаправление неудачных сообщений</li>
     *   <li>TTL: автоматическое удаление старых сообщений</li>
     * </ul>
     *
     * @return настроенная Queue instance
     */
    @Bean
    public Queue dataQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getQueueName())
                .withArgument("x-queue-type", rabbitMQProperties.getQueueType())
                .withArgument("x-delivery-limit", rabbitMQProperties.getDeliveryLimit())
                .withArgument("x-message-ttl", rabbitMQProperties.getMessageTTL())
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getDlExchangeName())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getDlRoutingKey())
                .build();
    }

    /**
     * Метод создает durable Dead Letter Queue для хранения неудачных сообщений.
     */
    @Bean
    public Queue dlDataQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getDlQueueName())
                .withArgument("x-queue-type", rabbitMQProperties.getDlQueueType())
                .withArgument("x-message-ttl", rabbitMQProperties.getDlMessageTTL())
                .build();
    }

    /**
     * Метод связывает S3 Minio event очередь с S3 Minio exchange используя routing key.
     *
     * @return Binding между очередью и exchange
     */
    @Bean
    public Binding s3EventBinding() {
        return BindingBuilder.bind(s3EventQueue())
                .to(s3EventExchange())
                .with(rabbitMQProperties.getS3EventRoutingKey());
    }

    /**
     * Метод связывает очередь с exchange используя routing key.
     *
     * @return Binding между очередью и exchange
     */
    @Bean
    public Binding dataQueueBinding() {
        return BindingBuilder.bind(dataQueue())
                .to(dataExchange())
                .with(rabbitMQProperties.getRoutingKey());
    }

    /**
     * Привязка DLQ к DLX.
     */
    @Bean
    public Binding dlDataQueueBinding() {
        return BindingBuilder.bind(dlDataQueue())
                .to(dlDataExchange())
                .with(rabbitMQProperties.getDlRoutingKey());
    }

    /**
     * Метод настраивает JSON конвертер для сериализации сообщений.
     *
     * <p>Конвертирует Java объекты в JSON и обратно для передачи через RabbitMQ.
     * Автоматически использует Jackson под капотом.</p>
     *
     * @return Jackson2JsonMessageConverter instance
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(customExceptionStrategy());
    }

    @Bean
    FatalExceptionStrategy customExceptionStrategy() {
        return new CustomExceptionStrategy();
    }

    /**
     * Метод настраивает фабрику контейнеров для слушателей сообщений.
     *
     * <p>Используется для @RabbitListener методов. Настраивает параллелизм
     * и обработку сообщений.</p>
     *
     * @param connectionFactory фабрика подключений RabbitMQ
     * @return настроенная фабрика контейнеров
     */
    @Bean("rabbitListenerContainerFactory")
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory
    (ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setErrorHandler(errorHandler());
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(errorHandler());
        return factory;
    }
}
