package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import ru.otus.hw.service.TuningService;

@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class TuningIntegrationConfig {

    private final TuningService tuningService;

    @Bean
    public MessageChannel tuningInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel snorkelChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel winchChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel underbodyChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel suspensionChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel tiresChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel tuningOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow tuningFlow() {
        return IntegrationFlow.from(tuningInputChannel())
                .handle(tuningService, "installSnorkel")
                .channel(snorkelChannel())

                .handle(tuningService, "installWinch")
                .channel(winchChannel())

                .handle(tuningService, "installProtection")
                .channel(underbodyChannel())

                .handle(tuningService, "upgradeSuspension")
                .channel(suspensionChannel())

                .handle(tuningService, "installBigTires")
                .get();
    }
}