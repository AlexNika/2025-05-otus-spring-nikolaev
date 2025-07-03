package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.logging.annotation.LogEntry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {

    private final TestConfig testConfig;

    private final TestFileNameProvider fileNameProvider;

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public List<Question> findAll() {
        try (InputStreamReader reader = this.getResourceFileAsInputStreamReader(fileNameProvider.getTestFileName())) {
            CsvToBean<QuestionDto> questionDtos = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withType(QuestionDto.class)
                    .withSeparator(testConfig.getDelimiter())
                    .withSkipLines(testConfig.getSkippedRows())
                    .build();
            return questionDtos
                    .parse()
                    .stream()
                    .map(QuestionDto::toDomainObject)
                    .toList();
        } catch (IOException e) {
            throw new QuestionReadException("The error occurred while reading file", e);
        }
    }

    private InputStreamReader getResourceFileAsInputStreamReader(String filename) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            return new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(filename)));
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new QuestionReadException("The error occurred while trying to get resource file " +
                                            "(file not found): {}", e);
        }
    }
}