package ru.otus.hw.config;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Locale;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "test")
public class AppProperties implements TestConfig, TestFileNameProvider, LocaleConfig {

    private final int rightAnswersCountToPass;

    private final Locale locale;

    @Getter(AccessLevel.NONE)
    private final Map<String, String> fileNameByLocaleTag;

    private final char delimiter;

    private final int skippedRows;

    @ConstructorBinding
    public AppProperties(int rightAnswersCountToPass, String locale, Map<String, String> fileNameByLocaleTag,
                         char delimiter, int skippedRows) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.locale = Locale.forLanguageTag(locale);
        this.fileNameByLocaleTag = fileNameByLocaleTag;
        this.delimiter = delimiter;
        this.skippedRows = skippedRows;
    }

    @Override
    public String getTestFileName() {
        return fileNameByLocaleTag.get(locale.toLanguageTag());
    }
}