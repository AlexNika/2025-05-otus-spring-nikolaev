package ru.otus.hw.config;

public interface TestConfig {
    int getRightAnswersCountToPass();

    char getDelimiter();

    int getSkippedRows();
}