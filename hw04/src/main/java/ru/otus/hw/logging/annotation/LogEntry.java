package ru.otus.hw.logging.annotation;

import org.springframework.boot.logging.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogEntry {

    LogLevel value() default LogLevel.INFO;

    ChronoUnit unit() default ChronoUnit.SECONDS;

    boolean showArgs() default false;

    boolean showResult() default false;

    boolean showExecutionTime() default true;
}

