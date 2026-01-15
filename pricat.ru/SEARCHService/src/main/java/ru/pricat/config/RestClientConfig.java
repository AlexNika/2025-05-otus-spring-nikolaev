package ru.pricat.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Конфигурационный класс для настройки REST клиентов.
 * Создает бины RestClient.Builder для HTTP запросов с поддержкой балансировки нагрузки через Eureka.
 */
@Configuration
public class RestClientConfig {

    /**
     * Создает RestClient.Builder с поддержкой балансировки нагрузки.
     * Этот клиент автоматически разрешает имена сервисов через Eureka Server
     * и распределяет нагрузку между экземплярами сервиса.
     *
     * @return RestClient.Builder с интеграцией Eureka и балансировкой нагрузки
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Создает обычный RestClient.Builder без балансировки нагрузки.
     * Используется для прямых HTTP вызовов по конкретным URL.
     *
     * @return Стандартный RestClient.Builder для прямых HTTP соединений
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}