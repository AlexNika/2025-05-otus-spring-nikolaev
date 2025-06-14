package service;

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
import ru.otus.hw.service.IOService;
import ru.otus.hw.service.TestServiceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    }

    @DisplayName("Should get questions from QuestionDao and print them with multiple choice answers")
    @Test
    void executeTest() {
        given(questionDaoMock.findAll()).willReturn(expectedQuestions);
        willDoNothing().given(ioServiceMock).printLine(stringArgumentCaptor.capture());
        willDoNothing().given(ioServiceMock).printFormattedLine(stringArgumentCaptor.capture());

        testService.executeTest();

        verify(questionDaoMock, times(1)).findAll();
        verify(ioServiceMock, times(3)).printLine(any(String.class));
        verify(ioServiceMock, times(1)).printFormattedLine(any(String.class));

        List<String> actualPrintedLines = stringArgumentCaptor.getAllValues();
        assertThat(actualPrintedLines).hasSize(4);
    }
}
