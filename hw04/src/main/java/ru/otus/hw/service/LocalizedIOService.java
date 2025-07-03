package ru.otus.hw.service;

import ru.otus.hw.logging.annotation.LogEntry;

import java.time.temporal.ChronoUnit;

public interface LocalizedIOService extends LocalizedMessagesService, IOService {
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    void printLineLocalized(String code);

    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    void printFormattedLineLocalized(String code, Object... args);

    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    String readStringWithPromptLocalized(String promptCode);

    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    int readIntForRangeLocalized(int min, int max, String errorMessageCode);

    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    int readIntForRangeWithPromptLocalized(int min, int max, String promptCode, String errorMessageCode);
}
