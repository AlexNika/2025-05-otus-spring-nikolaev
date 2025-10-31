package ru.pricat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "localDateTimeProvider")
public class AuditingConfig {

    @Bean
    public DateTimeProvider localDateTimeProvider() {
        ZoneId systemZoneId = ZoneId.systemDefault();
        Clock clock = Clock.system(systemZoneId);
        return () -> Optional.of(Instant.now(clock));
    }
}
