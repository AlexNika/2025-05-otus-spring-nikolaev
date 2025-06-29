package ru.otus.hw.logging;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Aspect
@Component
public class LogEntryAspect {

    @Around("@annotation(ru.otus.hw.logging.annotation.LogEntry)")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        var codeSignature = (CodeSignature) point.getSignature();
        var methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        AnnotationParams annotationParams = getAnnotationParams(method);
        String methodName = method.getName();
        Object[] methodArgs = point.getArgs();
        String[] methodParams = codeSignature.getParameterNames();
        log(logger, annotationParams.level(), entry(methodName, annotationParams.showArgs(), methodParams, methodArgs));
        FinalResult finalResult = launchPointAndGetDurationMetric(point, annotationParams.unit());
        log(logger, annotationParams.level(), exit(methodName, finalResult.duration(), finalResult.response(),
                annotationParams.showResult(), annotationParams.showExecutionTime()));
        return finalResult.response();
    }

    static String entry(String methodName, boolean showArgs, String[] params, Object[] args) {
        StringJoiner message = new StringJoiner(" ")
                .add("Started")
                .add(methodName)
                .add("method");
        if (isShowArgsParamsAndArgs(showArgs, params, args)) {
            Map<String, Object> values = IntStream.range(0, params.length)
                    .boxed()
                    .collect(Collectors
                            .toMap(i -> params[i],
                                    i -> args[i],
                                    (a, b) -> b,
                                    () -> new HashMap<>(params.length)));
            log.info("!!! -> values: {}", values);
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

    private static AnnotationParams getAnnotationParams(Method method) {
        var annotation = method.getAnnotation(LogEntry.class);
        LogLevel level = annotation.value();
        ChronoUnit unit = annotation.unit();
        boolean showArgs = annotation.showArgs();
        boolean showResult = annotation.showResult();
        boolean showExecutionTime = annotation.showExecutionTime();
        return new AnnotationParams(level, unit, showArgs, showResult, showExecutionTime);
    }

    private static FinalResult launchPointAndGetDurationMetric(ProceedingJoinPoint point, ChronoUnit chronoUnit)
            throws Throwable {
        var start = Instant.now();
        var response = point.proceed();
        var end = Instant.now();
        var duration = String.format("%s %s", chronoUnit.between(start, end),
                chronoUnit.name().toLowerCase());
        return new FinalResult(response, duration);
    }

    private record FinalResult(Object response, String duration) {
    }

    private record AnnotationParams(LogLevel level, ChronoUnit unit, boolean showArgs, boolean showResult,
                                    boolean showExecutionTime) {
    }

    private static boolean isShowArgsParamsAndArgs(boolean showArgs, String[] params, Object[] args) {
        return showArgs && Objects.nonNull(params) && Objects.nonNull(args) && params.length == args.length;
    }
}
