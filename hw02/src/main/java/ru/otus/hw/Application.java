package ru.otus.hw;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.otus.hw.service.TestRunnerService;

@PropertySource("classpath:application.properties")
@Configuration
@ComponentScan
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(Application.class);
        TestRunnerService testRunnerService = context.getBean(TestRunnerService.class);
        testRunnerService.run();
    }
}