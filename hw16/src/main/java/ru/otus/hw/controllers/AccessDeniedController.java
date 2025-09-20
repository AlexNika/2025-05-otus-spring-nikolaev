package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorText",
                "Доступ запрещен! У вас нет прав для доступа к этой странице.");
        return "error";
    }
}