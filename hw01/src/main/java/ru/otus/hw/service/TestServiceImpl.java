package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        this.soutHeader();
        List<Question> questions = questionDao.findAll();
        this.soutQuestions(questions);
    }

    private void soutHeader() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
    }

    private void soutQuestions(List<Question> questions) {
        try {
            questions.forEach(question -> {
                ioService.printFormattedLine("Question: %s", question.text());
                ioService.printLine("Answers:");
                question.answers().forEach(answer ->
                        ioService.printFormattedLine("\t- %s", answer.text()));
                ioService.printLine("");
            });
        } catch (NullPointerException e) {
            log.error("Object passed as the parameter is null: {}", e.getMessage());
        }
    }
}

