package ru.otus.hw.controllers;

import jakarta.annotation.Nullable;
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
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;
import java.util.Map;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/books/{bookId}/comments")
    public String getAllCommentsByBookId(@PathVariable("bookId") Long bookId,
                                         @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                         final String referrer,
                                         Model model) {
        BookDto book = bookService.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        List<CommentDto> comments = commentService.findByBookId(bookId);
        model.addAttribute("id", bookId);
        model.addAttribute("bookTitle", book.title());
        model.addAttribute("comments", comments);
        model.addAttribute("previousUrl", referrer);
        return "comments";
    }

    @SuppressWarnings("unused")
    @GetMapping("/books/{bookId}/comments/{commentId}/details")
    public String getComment(@PathVariable("bookId") Long bookId,
                             @PathVariable("commentId") Long commentId,
                             @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                             Model model) {
        CommentDto comment = commentService.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), commentId)));
        model.addAttribute("comment", comment);
        model.addAttribute("previousUrl", referrer);
        return "comment-view";
    }

    @GetMapping({"/books/{bookId}/comments/add", "/books/{bookId}/comments/{commentId}/edit"})
    public String showCommentForm(@PathVariable(name = "bookId") Long bookId,
                                  @PathVariable(name = "commentId", required = false) Long commentId,
                                  @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                                  Model model) {
        FormConfig config = prepareFormConfig(bookId, commentId);
        bookService.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        CommentDto comment = config.isUpdate()
                ? commentService.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), commentId)))
                : CommentDto.builder().build();
        prepareModelData(model, comment, List.of(referrer, config.formAction(), config.formTitle()));
        return "comment-upsert";
    }

    @PostMapping({"/books/{bookId}/comments/add", "/books/{bookId}/comments/{commentId}/edit"})
    public String saveOrUpdateComment(@PathVariable Map<String, String> pathVariables,
                                      @Valid @ModelAttribute("comment") CommentDto comment,
                                      BindingResult bindingResult,
                                      @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                          final String referrer,
                                      Model model) {
        IdPair ids = extractIdsFromPathVariables(pathVariables);
        CommentController.FormConfig config = prepareFormConfig(ids.bookId(), ids.commentId());
        String action = config.isUpdate() ? "updating existing" : "saving new";
        if (bindingResult.hasErrors()) {
            log.error("An error occurred while {} comment: {}", action, bindingResult.getFieldError());
            prepareModelData(model, comment, List.of(referrer, config.formAction(), config.formTitle()));
            return "comment-upsert";
        }
        try {
            saveComment(config, ids.commentId(), ids.bookId(), comment);
            return "redirect:/books/" + ids.bookId() + "/comments";
        } catch (Exception e) {
            log.error("An error occurred while {} comment: {}", action, e.getMessage());
            model.addAttribute("errorMessage",
                    "Ошибка при " + (config.isUpdate() ? "обновлении" : "сохранении") + " комментария: "
                    + e.getMessage());
            prepareModelData(model, comment, List.of(referrer, config.formAction(), config.formTitle()));
            return "comment-upsert";
        }
    }

    @SuppressWarnings("unused")
    @DeleteMapping("/books/{bookId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable(value = "bookId") Long bookId,
                                @PathVariable("commentId") Long commentId,
                                @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer) {
        commentService.deleteById(commentId);
        return "redirect:" + referrer;
    }

    private void saveComment(FormConfig config, @Nullable Long commentId, Long bookId, CommentDto comment) {
        if (config.isUpdate()) {
            commentService.update(commentId, comment.text());
        } else {
            commentService.insert(comment.text(), bookId);
        }
    }

    private void prepareModelData(Model model, CommentDto comment, List<String> formAttributes) {
        model.addAttribute("comment", comment);
        model.addAttribute("previousUrl", formAttributes.get(0));
        model.addAttribute("formAction", formAttributes.get(1));
        model.addAttribute("formTitle", formAttributes.get(2));
    }

    private FormConfig prepareFormConfig(Long bookId, @Nullable Long commentId) {
        boolean isUpdate = commentId != null;
        String formActionPrefix = "/books/" + bookId + "/comments/";
        String formAction = isUpdate ? formActionPrefix + commentId + "/edit" : formActionPrefix + "add";
        String formTitle = isUpdate ? "Редактирование комментария" : "Создание нового комментария";
        return new FormConfig(formAction, formTitle, isUpdate);
    }

    private IdPair extractIdsFromPathVariables(Map<String, String> pathVariables) {
        Long bookId = Long.valueOf(pathVariables.get("bookId"));
        String commentIdStr = pathVariables.get("commentId");
        Long commentId = (commentIdStr != null) ? Long.valueOf(commentIdStr) : null;
        return new IdPair(bookId, commentId);
    }

    private record FormConfig(String formAction, String formTitle, boolean isUpdate) {
    }

    private record IdPair(Long bookId, @Nullable Long commentId) {
    }
}
