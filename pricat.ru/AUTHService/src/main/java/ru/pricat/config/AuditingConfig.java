package ru.pricat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Конфигурационный класс для настройки аудита сущностей в R2DBC.
 * Включает аудит с помощью {@link EnableR2dbcAuditing} и предоставляет
 * бин {@link DateTimeProvider} для генерации временных меток.
 */
@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "localDateTimeProvider")
public class AuditingConfig {

    /**
     * Создает бин {@link DateTimeProvider}, который предоставляет
     * текущее время в виде {@link Instant}, используя системную временную зону.
     * Этот бин используется Spring Data R2DBC для заполнения полей,
     * аннотированных @CreatedDate и @LastModifiedDate.
     *
     * @return провайдер даты и времени, возвращающий текущий момент времени
     */
    @Bean
    public DateTimeProvider localDateTimeProvider() {
        ZoneId systemZoneId = ZoneId.systemDefault();
        Clock clock = Clock.system(systemZoneId);
        return () -> Optional.of(Instant.now(clock));
    }
}
