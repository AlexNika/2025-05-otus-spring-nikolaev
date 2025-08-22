package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GenrePageController {

    private final GenreService genreService;

    @GetMapping("/genres")
    public String getAllGenres(@RequestHeader(value = HttpHeaders.REFERER, required = false)
                                   final String referrer,
                               Model model) {
        model.addAttribute("previousUrl", referrer);
        return "genres";
    }

    @GetMapping("/genres/{id}/details")
    public String getGenreDetails(@RequestHeader(value = HttpHeaders.REFERER, required = false)
                                      final String referrer,
                                  @PathVariable("id") Long id,
                                  Model model) {
        genreService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Genre.class.getSimpleName(), id)));
        model.addAttribute("genreId", id);
        model.addAttribute("previousUrl", referrer);
        return "genre-view";
    }

    @GetMapping("/genres/add")
    public String showAddGenreForm(@RequestHeader(value = HttpHeaders.REFERER, required = false)
                                       final String referrer,
                                   Model model) {
        GenreDto genre = GenreDto.builder().build();
        FormModelAttributes attributes = new FormModelAttributes(
                referrer,
                "/api/v1/genres",
                "Создание нового жанра",
                false,
                null
        );
        prepareFormModel(model, genre, attributes);
        return "genre-upsert";
    }

    @GetMapping("/genres/{id}/edit")
    public String showEditGenreForm(@PathVariable("id") Long id,
                                    @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                        final String referrer,
                                    Model model) {
        GenreDto genre = genreService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Genre.class.getSimpleName(), id)));
        FormModelAttributes attributes = new FormModelAttributes(
                referrer,
                "/api/v1/genres/" + id,
                "Редактирование жанра",
                true,
                id
        );
        prepareFormModel(model, genre, attributes);
        return "genre-upsert";
    }

    private void prepareFormModel(Model model, GenreDto genre, FormModelAttributes attributes) {
        model.addAttribute("genre", genre);
        model.addAttribute("previousUrl", attributes.referrer());
        model.addAttribute("formAction", attributes.formAction());
        model.addAttribute("formTitle", attributes.formTitle());
        model.addAttribute("isUpdate", attributes.isUpdate());
        model.addAttribute("genreId", attributes.genreId());
    }

    private record FormModelAttributes(String referrer, String formAction, String formTitle, boolean isUpdate,
                                       Long genreId) {
    }
}