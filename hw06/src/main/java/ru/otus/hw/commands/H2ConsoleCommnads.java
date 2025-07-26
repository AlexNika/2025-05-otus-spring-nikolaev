package ru.otus.hw.commands;

import org.h2.tools.Console;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.sql.SQLException;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
public class H2ConsoleCommnads {
    @ShellMethod(value = "Launch H2 database console", key = "h2c")
    public void launchH2DataBaseConsole() {
        try {
            Console.main();
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
