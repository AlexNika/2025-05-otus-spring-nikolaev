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
public class GenrePageController {

    @GetMapping("/genres")
    public String getAllGenres() {
        return "genres";
    }

    @GetMapping("/genres/{id}/details")
    public String getGenreDetailsPage(@PathVariable Long id,
                                      @RequestHeader(value = REFERER, required = false) String referrer,
                                      Model model) {
        model.addAttribute("genreId", id);
        model.addAttribute("previousUrl", referrer);
        return "genre-view";
    }

    @GetMapping("/genres/add")
    public String showAddGenreForm(@RequestHeader(value = REFERER, required = false) String referrer,
                                   Model model) {
        model.addAttribute("previousUrl", referrer);
        return "genre-upsert";
    }

    @GetMapping("/genres/{id}/edit")
    public String showEditGenreForm(@PathVariable Long id,
                                    @RequestHeader(value = REFERER, required = false) String referrer,
                                    Model model) {
        model.addAttribute("genreId", id);
        model.addAttribute("isUpdate", true);
        model.addAttribute("previousUrl", referrer);
        return "genre-upsert";
    }
}