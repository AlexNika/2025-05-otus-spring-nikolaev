package ru.pricat.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Конфигурационный класс для настройки WebClient.
 * Определяет бины для создания WebClient с поддержкой балансировки нагрузки
 * и без неё, используя Spring Cloud LoadBalancer.
 */
@Configuration
public class WebClientConfig {

    /**
     * Создает бин {@link WebClient.Builder}, аннотированный {@link LoadBalanced}.
     * Этот билдер может быть использован для создания WebClient, который
     * автоматически использует Spring Cloud LoadBalancer для разрешения
     * имен сервисов и распределения запросов между их экземплярами.
     *
     * @return настроенный билдер WebClient с поддержкой балансировки нагрузки
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Создает бин {@link WebClient.Builder} без поддержки балансировки нагрузки.
     * Этот билдер может быть использован для создания WebClient с прямым указанием
     * URL или в сценариях, где балансировка не требуется.
     *
     * @return базовый билдер WebClient
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
