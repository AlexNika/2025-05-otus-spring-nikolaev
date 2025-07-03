package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.logging.annotation.LogEntry;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public TestResult executeTestFor(Student student) {
        this.soutHeader();
        TestResult testResult = new TestResult(student);
        List<Question> questions = questionDao.findAll();
        questions.forEach(question -> testProcess(question, testResult));
        return testResult;
    }

    private void testProcess(Question question, TestResult testResult) {
        soutQuestion(question);
        ioService.printLine("");
        Boolean isAnswerCorrect = getAnswerAndCheckValidation(question);
        testResult.applyAnswer(question, isAnswerCorrect);
    }

    private void soutHeader() {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");
    }

    private void soutQuestion(Question question) {
        ioService.printFormattedLineLocalized("TestService.question", question.text());
        ioService.printLineLocalized("TestService.answer");
        List<Answer> answers = question.answers();
        IntStream.range(1, answers.size() + 1)
                .forEach(i -> ioService.printFormattedLine("\t%d. %s", i, answers.get(i - 1).text()));
    }

    private Boolean getAnswerAndCheckValidation(Question question) {
        int answerNumber = ioService.readIntForRangeWithPromptLocalized(0, question.answers().size(),
                "TestService.enter.correct.answer.number",
                "TestService.response.not.correct");
        ioService.printFormattedLineLocalized("TestService.confirmation.enter.number", answerNumber);
        ioService.printLine("");
        return question.answers().get(answerNumber - 1).isCorrect();
    }
}