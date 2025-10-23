package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.config.CircuitBreakerMonitorConfig;

import java.util.Map;

@RestController
@RequestMapping("/api/circuit-breaker")
@RequiredArgsConstructor
public class CircuitBreakerController {

    private final CircuitBreakerMonitorConfig circuitBreakerMonitorConfig;

    @GetMapping("/status")
    public Map<String, Object> getCircuitBreakersStatus() {
        return circuitBreakerMonitorConfig.getCircuitBreakerStatus();
    }
}