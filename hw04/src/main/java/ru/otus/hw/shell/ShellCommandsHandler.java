package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.security.LoginContext;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.shell.interactive.enabled", havingValue = "true")
public class ShellCommandsHandler {

    private final LoginContext loginContext;

    private final LocalizedIOService ioService;

    private final TestRunnerService testRunnerService;

    @ShellMethod(value = "Login command", key = {"login", "l"})
    public void login(@ShellOption(defaultValue = "AnyUser") String userName) {
        loginContext.login(userName);
        ioService.printFormattedLineLocalized("ShellCommandHandler.login.welcome", userName);
    }

    @ShellMethod(value = "Run test command", key = {"run", "r"})
    @ShellMethodAvailability(value = "isLaunchCommandAvailable")
    public void launch() {
        testRunnerService.run();
        ioService.printLineLocalized("ShellCommandHandler.test.run.completed");
    }

    private Availability isLaunchCommandAvailable() {
        return loginContext.isUserLoggedIn()
                ? Availability.available()
                : Availability.unavailable(ioService.getMessage("ShellCommandHandler.login.first"));
    }
}
