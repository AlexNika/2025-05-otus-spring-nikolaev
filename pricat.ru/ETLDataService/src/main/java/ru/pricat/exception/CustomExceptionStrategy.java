package ru.pricat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

@Slf4j
public class CustomExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    @Override
    public boolean isFatal(Throwable throwable) {
        if (throwable.getCause() instanceof ItemException) {
            log.error("Exception has occurred : {} ", throwable.getMessage());
            return true;
        }
        return false;
    }
}
