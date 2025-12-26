package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.REFERER;

@Controller
@RequiredArgsConstructor
public class AuthorPageController {

    @GetMapping("/authors")
    public String getAllAuthors() {
        return "authors";
    }

    @GetMapping("/authors/{id}/details")
    public String getAuthorDetailsPage(@PathVariable Long id,
                                       @RequestHeader(value = REFERER, required = false) String referrer,
                                       Model model) {
        model.addAttribute("authorId", id);
        model.addAttribute("previousUrl", referrer);
        return "author-view";
    }

    @GetMapping("/authors/add")
    public String showAddAuthorForm(@RequestHeader(value = REFERER, required = false) String referrer,
                                    Model model) {
        model.addAttribute("previousUrl", referrer);
        return "author-upsert";
    }

    @GetMapping("/authors/{id}/edit")
    public String showEditAuthorForm(@PathVariable Long id,
                                     @RequestHeader(value = REFERER, required = false) String referrer,
                                     Model model) {
        model.addAttribute("authorId", id);
        model.addAttribute("isUpdate", true);
        model.addAttribute("previousUrl", referrer);
        return "author-upsert";
    }
}