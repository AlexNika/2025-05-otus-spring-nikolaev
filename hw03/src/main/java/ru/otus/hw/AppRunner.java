package ru.otus.hw;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.otus.hw.logging.annotation.LogEntry;
import ru.otus.hw.service.TestRunnerService;

import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class AppRunner implements ApplicationRunner {

    private final TestRunnerService testRunnerService;

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void run(ApplicationArguments args) {
        testRunnerService.run();
    }
}
