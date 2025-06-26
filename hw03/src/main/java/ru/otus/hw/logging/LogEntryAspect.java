package ru.otus.hw.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import ru.otus.hw.logging.annotation.LogEntry;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
public class LogEntryAspect {

    @Around("@annotation(ru.otus.hw.logging.annotation.LogEntry)")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        var codeSignature = (CodeSignature) point.getSignature();
        var methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        var annotation = method.getAnnotation(LogEntry.class);
        LogLevel level = annotation.value();
        ChronoUnit unit = annotation.unit();
        boolean showArgs = annotation.showArgs();
        boolean showResult = annotation.showResult();
        boolean showExecutionTime = annotation.showExecutionTime();
        String methodName = method.getName();
        Object[] methodArgs = point.getArgs();
        String[] methodParams = codeSignature.getParameterNames();
        log(logger, level, entry(methodName, showArgs, methodParams, methodArgs));
        var start = Instant.now();
        var response = point.proceed();
        var end = Instant.now();
        var duration = String.format("%s %s", unit.between(start, end), unit.name().toLowerCase());
        log(logger, level, exit(methodName, duration, response, showResult, showExecutionTime));
        return response;
    }

    static String entry(String methodName, boolean showArgs, String[] params, Object[] args) {
        StringJoiner message = new StringJoiner(" ")
                .add("Started")
                .add(methodName)
                .add("method");
        if (showArgs && Objects.nonNull(params) && Objects.nonNull(args) && params.length == args.length) {
            Map<String, Object> values = IntStream.range(0, params.length)
                    .boxed()
                    .collect(Collectors
                            .toMap(i -> params[i],
                                    i -> args[i],
                                    (a, b) -> b,
                                    () -> new HashMap<>(params.length)));
            message.add("with args:")
                    .add(values.toString());
        }
        return message.toString();
    }

    static String exit(String methodName, String duration, Object result, boolean showResult,
                       boolean showExecutionTime) {
        StringJoiner message = new StringJoiner(" ")
                .add("Finished")
                .add(methodName)
                .add("method");
        if (showExecutionTime) {
            message.add("in")
                    .add(duration);
        }
        if (showResult && result != null) {
            message.add("with return:")
                    .add(result.toString());
        }
        return message.toString();
    }

    static void log(Logger logger, LogLevel level, String message) {
        switch (level) {
            case DEBUG -> logger.debug(message);
            case TRACE -> logger.trace(message);
            case WARN -> logger.warn(message);
            case ERROR, FATAL -> logger.error(message);
            default -> logger.info(message);
        }
    }
}
