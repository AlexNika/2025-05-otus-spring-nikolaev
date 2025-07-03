package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.otus.hw.logging.annotation.LogEntry;

import java.time.temporal.ChronoUnit;

@Service
@Primary
@RequiredArgsConstructor
public class LocalizedIOServiceImpl implements LocalizedIOService {

    private final LocalizedMessagesService localizedMessagesService;

    private final IOService ioService;

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void printLine(String s) {
        ioService.printLine(s);
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void printFormattedLine(String s, Object... args) {
        ioService.printFormattedLine(s, args);
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public String readString() {
        return ioService.readString();
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public String readStringWithPrompt(String prompt) {
        return ioService.readStringWithPrompt(prompt);
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public int readIntForRange(int min, int max, String errorMessage) {
        return ioService.readIntForRange(min, max, errorMessage);
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public int readIntForRangeWithPrompt(int min, int max, String prompt, String errorMessage) {
        return ioService.readIntForRangeWithPrompt(min, max, prompt, errorMessage);
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void printLineLocalized(String code) {
        ioService.printLine(localizedMessagesService.getMessage(code));
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void printFormattedLineLocalized(String code, Object... args) {
        ioService.printLine(localizedMessagesService.getMessage(code, args));
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public String readStringWithPromptLocalized(String promptCode) {
        return ioService.readStringWithPrompt(localizedMessagesService.getMessage(promptCode));
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public int readIntForRangeLocalized(int min, int max, String errorMessageCode) {
        return ioService.readIntForRange(min, max, localizedMessagesService.getMessage(errorMessageCode));
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public int readIntForRangeWithPromptLocalized(int min, int max, String promptCode, String errorMessageCode) {
        return ioService.readIntForRangeWithPrompt(min, max,
                localizedMessagesService.getMessage(promptCode),
                localizedMessagesService.getMessage(errorMessageCode)
        );
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public String getMessage(String code, Object... args) {
        return localizedMessagesService.getMessage(code, args);
    }
}
