package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.logging.annotation.LogEntry;

import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final StudentService studentService;

    private final ResultService resultService;

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void run() {
        try {
            Student student = studentService.determineCurrentStudent();
            TestResult testResult = testService.executeTestFor(student);
            resultService.showResult(testResult);
        } catch (QuestionReadException e) {
            log.error("The error occurred while reading questions: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage());
        }
    }
}