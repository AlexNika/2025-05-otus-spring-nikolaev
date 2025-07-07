package ru.otus.hw.security;

import org.springframework.stereotype.Component;
import ru.otus.hw.logging.annotation.LogEntry;

import java.time.temporal.ChronoUnit;

import static java.util.Objects.nonNull;

@Component
public class InMemoryLoginContext implements LoginContext {
    private String userName;

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public void login(String userName) {
        this.userName = userName;
    }

    @Override
    @LogEntry(showArgs = true, showResult = true, unit = ChronoUnit.MILLIS)
    public boolean isUserLoggedIn() {
        return nonNull(userName);
    }
}
