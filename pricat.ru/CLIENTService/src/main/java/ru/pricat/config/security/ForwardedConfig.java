package ru.pricat.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Конфигурационный класс для обработки заголовков X-Forwarded-*.
 * Позволяет Spring Boot корректно формировать URL для редиректов
 * на основе заголовков, установленных api-gateway.
 */
@Configuration
public class ForwardedConfig {

    /**
     * Создаёт фильтр для обработки заголовков X-Forwarded-*.
     * Обеспечивает корректное формирование URL при редиректах.
     *
     * @return ForwardedHeaderFilter
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
