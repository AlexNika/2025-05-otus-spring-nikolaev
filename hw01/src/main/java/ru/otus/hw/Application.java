package ru.otus.hw;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.otus.hw.service.TestRunnerService;

@Slf4j
public class Application {
    public static void main(String[] args) {

        try (ConfigurableApplicationContext context =
                     new ClassPathXmlApplicationContext("/spring-context.xml")) {
            TestRunnerService testRunnerService = context.getBean(TestRunnerService.class);
            testRunnerService.run();
        } catch (BeansException e) {
            log.error("The error occurred when trying to create context from \"spring-context.xml\" file: {}",
                    e.getMessage());
        }
    }
}