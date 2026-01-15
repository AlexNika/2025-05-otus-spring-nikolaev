package ru.pricat.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ru.pricat.util.AppConstants.API_V1_CLIENT_PATH;

@RestController
@RequestMapping(API_V1_CLIENT_PATH)
public class DebugController {

    @GetMapping("/debug-headers")
    public String debugHeaders(HttpServletRequest request) {
        return "Request URL: " + request.getRequestURL().toString() + "\n" +
               "X-Forwarded-Host: " + request.getHeader("X-Forwarded-Host") + "\n" +
               "X-Forwarded-Proto: " + request.getHeader("X-Forwarded-Proto") + "\n" +
               "X-Forwarded-Port: " + request.getHeader("X-Forwarded-Port") + "\n" +
               "Host в запросе: " + request.getHeader("Host") + "\n" +
               "scheme: " + request.getScheme() + "\n" +
               "serverName: " + request.getServerName() + "\n" +
               "serverPort: " + request.getServerPort();
    }
}
