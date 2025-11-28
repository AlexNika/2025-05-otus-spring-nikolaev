package ru.pricat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Конфигурация Spring Data JPA.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "ru.pricat.repository")
@EnableTransactionManagement
public class JpaConfig {
}
