package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping("/genres")
    public String getAllGenres(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                               Model model) {
        List<GenreDto> genres = genreService.findAll();
        model.addAttribute("genres", genres);
        model.addAttribute("previousUrl", referrer);
        return "genres";
    }

    @GetMapping("/genres/{id}/details")
    public String getGenre(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                           @PathVariable("id") Long id,
                           Model model) {
        GenreDto genre = genreService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Genre.class.getSimpleName(), id)));
        model.addAttribute("genre", genre);
        model.addAttribute("previousUrl", referrer);
        return "genre-view";
    }

    @GetMapping({"/genres/add", "/genres/{id}/edit"})
    public String showGenreForm(@PathVariable(required = false) Long id,
                                @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                                Model model) {

        GenreController.FormConfig config = prepareFormConfig(id);

        String className = Genre.class.getSimpleName();
        GenreDto genre = config.isUpdate() ?
                genreService.findById(id).orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(className, id))) : GenreDto.builder().build();

        prepareModelData(model, genre, referrer, config.formAction(), config.formTitle());
        return "genre-upsert";
    }

    @PostMapping({"/genres/add", "/genres/{id}/edit"})
    public String saveOrUpdateGenre(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("genre") GenreDto genre,
            BindingResult bindingResult,
            @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
            Model model) {

        GenreController.FormConfig config = prepareFormConfig(id);
        String action = config.isUpdate() ? "updating existing" : "saving new";

        if (bindingResult.hasErrors()) {
            log.error("An error occurred while {} genre: {}", action, bindingResult.getFieldError());
            prepareModelData(model, genre, referrer, config.formAction(), config.formTitle());
            return "genre-upsert";
        }

        try {
            saveGenre(config, id, genre);
            return "redirect:/genres";
        } catch (Exception e) {
            log.error("An error occurred while {} genre: {}", action, e.getMessage());
            model.addAttribute("errorMessage",
                    "Ошибка при " + (config.isUpdate() ? "обновлении" : "сохранении") + " жанра: "
                    + e.getMessage());
            prepareModelData(model, genre, referrer, config.formAction(), config.formTitle());
            return "genre-upsert";
        }
    }

    @DeleteMapping("/genres/{id}/delete")
    public String deleteGenre(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                              @PathVariable("id") Long id) {
        genreService.deleteById(id);
        return "redirect:" + referrer;
    }

    private void saveGenre(GenreController.FormConfig config, Long id, GenreDto genre) {
        if (config.isUpdate()) {
            genreService.update(id, genre.name());
        } else {
            genreService.insert(genre.name());
        }
    }

    private void prepareModelData(Model model, GenreDto genre, String referrer, String formAction,
                                  String formTitle) {
        model.addAttribute("genre", genre);
        model.addAttribute("previousUrl", referrer);
        model.addAttribute("formAction", formAction);
        model.addAttribute("formTitle", formTitle);
    }

    private GenreController.FormConfig prepareFormConfig(Long id) {
        boolean isUpdate = id != null;
        String formAction = isUpdate ? "/genres/" + id + "/edit" : "/genres/add";
        String formTitle = isUpdate ? "Редактирование жанра" : "Создание нового жанра";
        return new GenreController.FormConfig(formAction, formTitle, isUpdate);
    }

    private record FormConfig(String formAction, String formTitle, boolean isUpdate) {
    }
}
