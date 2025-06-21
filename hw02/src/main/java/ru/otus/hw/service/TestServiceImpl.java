package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
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
        ioService.printFormattedLine("Please answer the questions below%n");
    }

    private void soutQuestion(Question question) {
        ioService.printFormattedLine("Question: %s", question.text());
        ioService.printLine("Answers:");
        List<Answer> answers = question.answers();
        IntStream.range(1, answers.size() + 1)
                .forEach(i -> ioService.printFormattedLine("\t%d. %s", i, answers.get(i - 1).text()));
    }

    private Boolean getAnswerAndCheckValidation(Question question) {
        int answerNumber = ioService.readIntForRangeWithPrompt(0, question.answers().size(),
                "Enter correct answer number",
                "Your response is not correct, please try again");
        ioService.printFormattedLine("You entered answer number: %d", answerNumber);
        ioService.printLine("");
        return question.answers().get(answerNumber - 1).isCorrect();
    }
}