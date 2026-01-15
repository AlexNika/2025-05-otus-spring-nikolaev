package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BookPageController {

    @GetMapping("/books")
    public String getAllBooks() {
        return "books";
    }

    @GetMapping("/books/{id}/details")
    public String getBookDetailsPage(@PathVariable Long id,
                                     @RequestHeader(value = HttpHeaders.REFERER, required = false) String referrer,
                                     Model model) {
        model.addAttribute("bookId", id);
        model.addAttribute("previousUrl", referrer);
        return "book-view";
    }

    @GetMapping("/books/add")
    public String showAddBookForm(@RequestHeader(value = HttpHeaders.REFERER, required = false) String referrer,
                                  Model model) {
        model.addAttribute("previousUrl", referrer);
        return "book-upsert";
    }

    @GetMapping("/books/{id}/edit")
    public String showEditBookForm(@PathVariable Long id,
                                   @RequestHeader(value = HttpHeaders.REFERER, required = false) String referrer,
                                   Model model) {
        model.addAttribute("bookId", id);
        model.addAttribute("isUpdate", true);
        model.addAttribute("previousUrl", referrer);
        return "book-upsert";
    }
}