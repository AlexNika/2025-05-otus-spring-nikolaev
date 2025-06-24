package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock
    private QuestionDao questionDaoMock;

    @Mock
    private IOService ioServiceMock;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @InjectMocks
    private TestServiceImpl testService;

    private List<Question> expectedQuestions;

    private Student student;

    private TestResult testResult;

    @BeforeEach
    void setUp() {
        String questionText = """
                Which word is reserved in the Python programming language for getting the return value from a function?
                """;
        Answer answer1 = new Answer("revert", false);
        Answer answer2 = new Answer("return", true);
        Answer answer3 = new Answer("reborn", false);
        Answer answer4 = new Answer("restart", false);
        Question question = new Question(questionText, List.of(answer1, answer2, answer3, answer4));
        expectedQuestions = List.of(question);
        student = new Student("Petr", "Petrov");
        testResult = new TestResult(student);
    }

    @DisplayName("Should get questions from QuestionDao than run test for student and check answer")
    @Test
    void executeTestFor() {
        given(questionDaoMock.findAll()).willReturn(expectedQuestions);
        given(ioServiceMock.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).willReturn(2);

        willDoNothing().given(ioServiceMock).printLine(stringArgumentCaptor.capture());
        willDoNothing().given(ioServiceMock).printFormattedLine(stringArgumentCaptor.capture());

        testResult = testService.executeTestFor(student);

        assertEquals(questionDaoMock.findAll().size(), testResult.getAnsweredQuestions().size());
        assertEquals(1, testResult.getRightAnswersCount());

        verify(questionDaoMock, times(2)).findAll();
        verify(ioServiceMock, times(4)).printLine(any(String.class));
        verify(ioServiceMock, times(1)).printFormattedLine(any(String.class));
        verify(ioServiceMock, times(1)).readIntForRangeWithPrompt(anyInt(), anyInt(),
                anyString(), anyString());

        List<String> actualPrintedLines = stringArgumentCaptor.getAllValues();
        assertThat(actualPrintedLines).hasSize(5);
    }
}