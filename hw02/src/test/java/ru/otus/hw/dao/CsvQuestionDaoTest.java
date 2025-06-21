package ru.otus.hw.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CsvQuestionDaoTest {

    private static final String CORRECT_FILENAME = "questions.csv";
    private static final String MISSING_FILENAME = "missing_file.csv";
    private static final char DELIMITER = ';';
    private static final int SKIPPED_ROWS = 1;

    @Mock
    TestFileNameProvider testFileNameProviderMock;

    @Mock
    TestConfig testConfigMock;

    @InjectMocks
    CsvQuestionDao csvQuestionDao;

    private List<Question> expectedQuestions;

    @BeforeEach
    void setUp() {
        String question1Text = "question1?";
        String question2Text = "question2?";
        Answer answer1_1 = new Answer("answer1(1)", false);
        Answer answer1_2 = new Answer("answer1(2)", true);
        Answer answer1_3 = new Answer("answer1(3)", false);

        Answer answer2_1 = new Answer("answer2(1)", true);
        Answer answer2_2 = new Answer("answer2(2)", false);
        Answer answer2_3 = new Answer("answer2(3)", false);
        Answer answer2_4 = new Answer("answer2(4)", false);

        Question question1 = new Question(question1Text, List.of(answer1_1, answer1_2, answer1_3));
        Question question2 = new Question(question2Text, List.of(answer2_1, answer2_2, answer2_3, answer2_4));
        expectedQuestions = List.of(question1, question2);
    }

    @DisplayName("Should correctly return questions with answers from questions.csv file")
    @Test
    void findAllCorrectFileTest() {
        given(testFileNameProviderMock.getTestFileName()).willReturn(CORRECT_FILENAME);
        given(testConfigMock.getDelimeter()).willReturn(DELIMITER);
        given(testConfigMock.getSkippedRows()).willReturn(SKIPPED_ROWS);

        List<Question> actualQuestions = csvQuestionDao.findAll();

        assertThat(actualQuestions).isNotNull().hasSize(expectedQuestions.size())
                .containsExactlyInAnyOrderElementsOf(expectedQuestions);
        assertThat(actualQuestions).isEqualTo(expectedQuestions);
        verify(testFileNameProviderMock, times(1)).getTestFileName();
    }

    @DisplayName("Should throw QuestionReadException exception when csv file is missing")
    @Test
    void findAllMissingFileTest() {
        given(testFileNameProviderMock.getTestFileName()).willReturn(MISSING_FILENAME);

        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("The error occurred while trying to get resource file " +
                                      "(file not found): {}");
        verify(testFileNameProviderMock, times(1)).getTestFileName();
    }
}
