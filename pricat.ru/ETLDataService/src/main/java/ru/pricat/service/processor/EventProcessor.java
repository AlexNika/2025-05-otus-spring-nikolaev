package ru.pricat.service.processor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

public interface EventProcessor {

    @EventListener(ApplicationReadyEvent.class)
    void start();

    void stop();
}
