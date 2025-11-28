package ru.pricat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;


/**
 * Конфигурационный класс для настройки временных зон в аудиторских полях сущностей.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class AuditConfig {


    /**
     * Создает и настраивает провайдер даты и времени для аудиторских полей сущностей.
     * @return {@link DateTimeProvider}, который предоставляет текущее время с учетом временной зоны
     */
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now(ZoneId.systemDefault()));
    }

    /**
     * Альтернативная версия провайдера с явным указанием временной зоны.
     *
     * @param zoneId строка с идентификатором временной зоны (например, "UTC", "Europe/Moscow")
     * @return {@link DateTimeProvider}, настроенный на указанную временную зону
     * @throws java.time.zone.ZoneRulesException если указанная зона не существует
     */
    public DateTimeProvider dateTimeProviderForZone(String zoneId) {
        ZoneId zone = ZoneId.of(zoneId);
        return () -> Optional.of(OffsetDateTime.now(zone));
    }
}
