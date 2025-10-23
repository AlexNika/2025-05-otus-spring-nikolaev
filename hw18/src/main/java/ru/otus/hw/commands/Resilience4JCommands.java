package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.config.CircuitBreakerMonitorConfig;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class Resilience4JCommands {

    private final CircuitBreakerMonitorConfig circuitBreakerMonitorConfig;

    @ShellMethod(value = "Show Resilience4J Circuit Breaker status", key = "cb_status")
    public void showCircuitBreakerStatus() {
        circuitBreakerMonitorConfig.getCircuitBreakerStatus()
                .forEach((key, value) -> log.info("Circuit breaker for: {} -> {}", key, value));
    }
}
