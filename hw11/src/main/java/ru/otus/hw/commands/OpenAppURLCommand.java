package ru.otus.hw.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;

@ShellComponent
public class OpenAppURLCommand {
    @ShellMethod(value = "Launch default web browser with index.html", key = "web")
    public void launchWebBrowser() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("rundll32 url.dll,FileProtocolHandler " + "http://localhost:8080/");
    }
}
