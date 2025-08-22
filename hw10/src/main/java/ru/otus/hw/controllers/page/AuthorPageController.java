package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthorPageController {

    private final AuthorService authorService;

    @GetMapping("/authors")
    public String getAllAuthors(@RequestHeader(value = HttpHeaders.REFERER, required = false)
                                    final String referrer,
                                Model model) {
        model.addAttribute("previousUrl", referrer);
        return "authors";
    }

    @GetMapping("/authors/{id}/details")
    public String getAuthorDetails(@RequestHeader(value = HttpHeaders.REFERER, required = false)
                                       final String referrer,
                                   @PathVariable("id") Long id,
                                   Model model) {
        authorService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)));

        model.addAttribute("authorId", id);
        model.addAttribute("previousUrl", referrer);
        return "author-view";
    }

    @GetMapping("/authors/add")
    public String showAddAuthorForm(@RequestHeader(value = HttpHeaders.REFERER, required = false)
                                        final String referrer,
                                    Model model) {
        AuthorDto author = AuthorDto.builder().build();
        FormModelAttributes attributes = new FormModelAttributes(
                referrer,
                "/api/v1/authors",
                "Создание нового автора",
                false,
                null
        );
        prepareFormModel(model, author, attributes);
        return "author-upsert";
    }

    @GetMapping("/authors/{id}/edit")
    public String showEditAuthorForm(@PathVariable("id") Long id,
                                     @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                        final String referrer,
                                     Model model) {
        AuthorDto author = authorService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)));
        FormModelAttributes attributes = new FormModelAttributes(
                referrer,
                "/api/v1/authors/" + id,
                "Редактирование автора",
                true,
                id
        );
        prepareFormModel(model, author, attributes);
        return "author-upsert";
    }

    private void prepareFormModel(Model model, AuthorDto author, FormModelAttributes attributes) {
        model.addAttribute("author", author);
        model.addAttribute("previousUrl", attributes.referrer());
        model.addAttribute("formAction", attributes.formAction());
        model.addAttribute("formTitle", attributes.formTitle());
        model.addAttribute("isUpdate", attributes.isUpdate());
        model.addAttribute("authorId", attributes.authorId());
    }

    private record FormModelAttributes(String referrer, String formAction, String formTitle, boolean isUpdate,
                                       Long authorId) {
    }
}