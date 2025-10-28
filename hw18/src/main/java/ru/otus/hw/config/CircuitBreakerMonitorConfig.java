package ru.otus.hw.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerMonitorConfig {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Bean
    public Map<String, Object> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbInfo = new HashMap<>();
            cbInfo.put("state", cb.getState());
            cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
            cbInfo.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbInfo.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
            status.put(cb.getName(), cbInfo);
        });
        return status;
    }
}
