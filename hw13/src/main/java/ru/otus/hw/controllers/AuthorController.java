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
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    private final BookService bookService;

    @GetMapping("/authors")
    public String getAllAuthors(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                                Model model) {
        Map<AuthorDto, Map<Long, String>> authorsAndTheirBooks = authorService.findAll().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        author -> bookService.findBooksByAuthorId(author.id()).stream()
                                .collect(Collectors.toMap(
                                        BookDto::id,
                                        BookDto::title
                                ))
                ));
        model.addAttribute("authorsAndTheirBooks", authorsAndTheirBooks);
        model.addAttribute("previousUrl", referrer);
        return "authors";
    }

    @GetMapping("/authors/{id}/details")
    public String getAuthor(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                            @PathVariable("id") Long id,
                            Model model) {
        AuthorDto author = authorService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)));
        List<BookDto> books = bookService.findBooksByAuthorId(author.id());
        model.addAttribute("author", author);
        model.addAttribute("books", books);
        model.addAttribute("previousUrl", referrer);
        return "author-view";
    }

    @GetMapping({"/authors/add", "/authors/{id}/edit"})
    public String showAuthorForm(@PathVariable(required = false) Long id,
                                 @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                                 Model model) {
        FormConfig config = prepareFormConfig(id);

        AuthorDto author = config.isUpdate()
                ? authorService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)))
                : AuthorDto.builder().build();

        prepareModelData(model, author, List.of(referrer, config.formAction(), config.formTitle()));
        return "author-upsert";
    }

    @PostMapping({"/authors/add", "/authors/{id}/edit"})
    public String saveOrUpdateAuthor(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("author") AuthorDto author,
            BindingResult bindingResult,
            @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
            Model model) {

        FormConfig config = prepareFormConfig(id);
        String action = config.isUpdate() ? "updating existing" : "saving new";

        if (bindingResult.hasErrors()) {
            log.error("An error occurred while {} author: {}", action, bindingResult.getFieldError());
            prepareModelData(model, author, List.of(referrer, config.formAction(), config.formTitle()));
            return "author-upsert";
        }

        try {
            saveAuthor(config, id, author);
            return "redirect:/authors";
        } catch (Exception e) {
            log.error("An error occurred while {} author: {}", action, e.getMessage());
            model.addAttribute("errorMessage",
                    "Ошибка при " + (config.isUpdate() ? "обновлении" : "сохранении") + " автора: " +
                    e.getMessage());
            prepareModelData(model, author, List.of(referrer, config.formAction(), config.formTitle()));
            return "author-upsert";
        }
    }

    @DeleteMapping("/authors/{id}/delete")
    public String deleteAuthor(@PathVariable("id") Long id) {
        authorService.deleteById(id);
        return "redirect:/authors";
    }

    private void saveAuthor(FormConfig config, Long id, AuthorDto author) {
        if (config.isUpdate()) {
            authorService.update(id, author.fullName());
        } else {
            authorService.insert(author.fullName());
        }
    }

    private void prepareModelData(Model model, AuthorDto author, List<String> formAttributes) {
        model.addAttribute("author", author);
        model.addAttribute("previousUrl", formAttributes.get(0));
        model.addAttribute("formAction", formAttributes.get(1));
        model.addAttribute("formTitle", formAttributes.get(2));
    }

    private FormConfig prepareFormConfig(Long id) {
        boolean isUpdate = id != null;
        String formAction = isUpdate ? "/authors/" + id + "/edit" : "/authors/add";
        String formTitle = isUpdate ? "Редактирование автора" : "Создание нового автора";
        return new FormConfig(formAction, formTitle, isUpdate);
    }

    private record FormConfig(String formAction, String formTitle, boolean isUpdate) {
    }
}
